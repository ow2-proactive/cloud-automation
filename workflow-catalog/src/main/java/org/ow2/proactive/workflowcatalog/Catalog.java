package org.ow2.proactive.workflowcatalog;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Catalog {
    private static final Logger logger = Logger.getLogger(Catalog.class.getName());
    private final Map<File, Workflow> workflows;
    private final UpdateTask updateTask;

    /**
     * @param path
     * @param refresh in milliseconds
     */
    public Catalog(File path, long refresh) {
        updateTask = new UpdateTask(path);
        workflows = new ConcurrentHashMap<File, Workflow>();

        forceUpdate();
        new Timer().schedule(updateTask, 0, refresh);
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
                        workflows.remove(file); // This workflow does not exist anymore
                        logger.info("Removed workflow : " + file.getName());
                    }
                }

                // Add new rules and update existing ones if needed
                for (File f : path.listFiles()) {
                    Workflow workflow = workflows.get(f);
                    if (workflow != null && workflow.hasChanged()) { // Known & modified
                        workflow.update();
                        logger.info("Updated workflow : " + f.getName());

                    } else if (workflow == null) { // Unknown
                        workflow = new Workflow(f);
                        workflow.update();
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

