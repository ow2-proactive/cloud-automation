package org.ow2.proactive.brokering;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Catalog {
    private static final Logger logger = Logger.getLogger(Catalog.class.getName());
    private final Map<File, Workflow> workflows;
    private final Timer timer;

    public Catalog(File path) {
        logger.setLevel(Level.ALL);
        timer = new Timer();
        UpdateTask task = new UpdateTask(path);
        timer.schedule(task, 60 * 1000); // 1 minute
        workflows = new HashMap<File, Workflow>();
        task.run();
    }

    // Return a workflow for which all variables specified in 'map' are present (maybe there is more)

    /**
     * Select and configure the workflow corresponding to the given action and attributes.
     * To be selected, these criterias must be satisfied :
     * - workflow has a generic information named 'action' with corresponding value
     * - all generic informations specified in workflow must exist in attributes, including values
     * - all variables specified in workflow must exist in attributes
     * If multiple workflows matches, the first one is returned.
     *
     * @param attributes
     * @return
     * @throws Exception
     */
    public File getWorkflow(Attributes attributes, Rules rules) throws Exception {
        for (Workflow workflow : workflows.values()) {
            if (workflow.isValid(attributes)) {
                // Variable and Generic informations values are replaced with given attributes
                return workflow.configure(attributes, rules);
            }
        }

        return null;
    }

    private void show(String s, Map<String, String> map) {
        System.out.println(s);
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = map.get(key);
            System.out.println(key + " => " + value);
        }
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
                        logger.fine("Removed rule : " + file.getName());
                    }
                }

                // Add new rules and update existing ones if needed
                for (File f : path.listFiles()) {
                    Workflow workflow = workflows.get(f);
                    if (workflow != null && workflow.hasChanged()) {
                        workflow.update();
                        logger.fine("Update a workflow : " + f.getName());
                    } else {
                        workflow = new Workflow(f);
                        workflow.update();
                        workflows.put(f, workflow);
                        logger.fine("Added in catalog : " + f.getName());
                    }
                }
            }
        }
    }
}
