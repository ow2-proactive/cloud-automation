package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;


public class Catalog {
    private static final Logger logger = Logger.getLogger(Catalog.class.getName());
    private final Map<File, Workflow> workflows;
    private final Timer timer;

    /**
     * @param path
     * @param refresh in seconds
     */
    public Catalog(File path, long refresh) {
        timer = new Timer();
        UpdateTask task = new UpdateTask(path);
        timer.schedule(task, 0, refresh);
        workflows = new HashMap<File, Workflow>();
        task.run();
    }

    public Collection<Workflow> getWorkflows() {
        return workflows.values();
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
                        logger.info("Added in catalog : " + f.getName());
                    }
                }
            }
        }
    }
}
