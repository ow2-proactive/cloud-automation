package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class Rules {
    private static final Logger logger = Logger.getLogger(Rules.class.getName());
    private final List<File> rules;

    /**
     * @param path
     * @param refresh in seconds
     */
    public Rules(File path, long refresh) {
        Timer timer = new Timer();
        rules = new LinkedList<File>();
        timer.schedule(new UpdateTask(path), 0, refresh);
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
                    if (!rules.contains(f) && isValidRuleFile(f)) {
                        rules.add(f);
                        logger.info("Added rule : " + f.getName());
                    }
                }
            }
        }

        private Boolean isValidRuleFile(File f) {
            return (f.getName().endsWith("groovy"));
        }
    }
}
