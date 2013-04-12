package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Rules {
    private static final Logger logger = Logger.getLogger(Rules.class.getName());
    private final List<File> rules;
    private final Timer timer;

    public Rules(File path) {
        timer = new Timer();
        timer.schedule(new UpdateTask(path), 60 * 1000); // 1 minute
        rules = new LinkedList<File>();
    }

    public List<File> getMatchingRules(Attributes attributes) {
        List<File> result = new LinkedList<File>();
        GroovyClassLoader gcl = new GroovyClassLoader();
        for (File file : rules) {
            try {
                Class clazz = gcl.parseClass(file);
                Rule rule = (Rule) clazz.newInstance();
                if( rule.match(attributes)) {
                    result.add(file);
                }

            } catch (Throwable e) {
                logger.fine("Error when loading a rules file : " + file.getName());
            }
        }
        return result;
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
                        logger.fine("Removed rule : " + rule.getName());
                    }
                }

                // Add new rules
                for (File f : path.listFiles()) {
                    if (!rules.contains(f)) {
                        rules.add(f);
                        logger.fine("Added rule : " + f.getName());
                    }
                }
            }
        }
    }
}
