package org.ow2.proactive.brokering;

import java.io.File;
import java.util.*;
import org.apache.log4j.Logger;

public class Rules {
    private static final Logger logger = Logger.getLogger(Rules.class.getName());
    private final List<File> rules;
    private final Timer timer;

    /**
     *
     * @param path
     * @param refresh in seconds
     */
    public Rules(File path, long refresh) {
        timer = new Timer();
        timer.schedule(new UpdateTask(path), 0, refresh);
        rules = new LinkedList<File>();
    }

    public List<File> getRules() {
        return rules;
    }

    private class UpdateTask extends TimerTask {
        private File path;

        private UpdateTask(File path) {
            this.path = path;
        }

        @Override
        public void run() {
            // Periodically updates rules(new and modified existing rules)
            if (path.isDirectory()) {
                // Removing old rules (when corresponding files are removed)
                Iterator<File> iterator = rules.iterator();
                while (iterator.hasNext()) {
                    File rule = iterator.next();
                    if (!rule.isFile()) {
                        rules.remove(rule); // This rule does not exist anymore
                        logger.info("Removed rule : " + rule.getName());
                    }
                }

                // Add new rules
                for (File f : path.listFiles()) {
                    if (!rules.contains(f)) {
                        rules.add(f);
                        logger.info("Added rule : " + f.getName());
                    }
                }
            }
        }
    }
}
