package org.ow2.proactive.workflowcatalog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class FileCatalog implements Catalog {
    private static final Logger logger = Logger.getLogger(FileCatalog.class.getName());
    private static final WildcardFileFilter XML_FILTER = new WildcardFileFilter("*.xml");
    private final Map<File, Workflow> workflows;
    private final UpdateTask updateTask;
    private CatalogListener catalogListener;

    /**
     * @param refresh in milliseconds
     */
    public FileCatalog(File path, long refresh, CatalogListener catalogListener) {
        this.catalogListener = catalogListener;
        updateTask = new UpdateTask(path);
        workflows = new ConcurrentHashMap<File, Workflow>();

        forceUpdate();
        new Timer().schedule(updateTask, 0, refresh);
    }

    public FileCatalog(File path, long refresh) {
        this(path, refresh, new CatalogListener() {
            @Override
            public void added(Workflow addedWorkflow) {

            }

            @Override
            public void updated(Workflow updatedWorkflow) {

            }

            @Override
            public void removed(Workflow removedWorkflow) {

            }
        });
    }

    public Collection<Workflow> getWorkflows(WorkflowParameters filter) {
        ArrayList<Workflow> result = new ArrayList<Workflow>();
        for (Workflow w: getWorkflows())
            if (filter.matches(w))
                result.add(w);
        logger.info("Matching workflows: " + result.size());
        return result;
    }

    public Collection<Workflow> getWorkflows() {
        return workflows.values();
    }

    private class UpdateTask extends TimerTask {
        private File path;

        private UpdateTask(File path) {
            this.path = path;
        }

        @Override
        public void run() {
            // Periodically updates catalog workflows (new and modified existing workflows)
            if (path.isDirectory()) {

                // Removing old workflows (when corresponding files are removed)
                Iterator<File> iterator = workflows.keySet().iterator();
                while (iterator.hasNext()) {
                    File file = iterator.next();
                    if (!file.isFile()) {
                        Workflow workflow = workflows.remove(file);// This workflow does not exist anymore
                        catalogListener.removed(workflow);
                        logger.info("Removed workflow : " + file.getName());
                    }
                }

                // Add new rules and update existing ones if needed
                for (File f : FileUtils.listFiles(path, XML_FILTER, TrueFileFilter.INSTANCE)) {
                    Workflow workflow = workflows.get(f);
                    if (workflow != null && workflow.hasChanged()) { // Known & modified
                        workflow.update();
                        catalogListener.updated(workflow);
                        logger.info("Updated workflow : " + f.getName());

                    } else if (workflow == null) { // Unknown
                        workflow = new Workflow(f);
                        workflow.update();
                        catalogListener.added(workflow);
                        workflows.put(f, workflow);
                        logger.info("Added to catalog : " + f.getName());
                    }
                }

                if (workflows.isEmpty()) {
                    logger.warn("Catalog is empty");
                }
            } else {
                logger.warn("Catalog not a directory");
            }
        }
    }

    public void forceUpdate() {
        updateTask.run();
    }
}

