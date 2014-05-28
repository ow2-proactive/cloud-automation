package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Scripts {

    protected static Logger logger = Logger.getLogger(Scripts.class.getName());
    protected final List<File> scripts;
    protected final File scriptsPath;

    public Scripts(File path, long refreshMs) {
        scripts = new LinkedList<File>();
        scriptsPath = path;

        Timer timer = new Timer();
        timer.schedule(new UpdateTask(scriptsPath), 0, refreshMs);
    }

    public List<File> getScripts() {
        return scripts;
    }

    public File getScript(String name) throws FileNotFoundException {
        Iterator<File> iterator = scripts.iterator();
        while (iterator.hasNext()) {
            File f = iterator.next();
            logger.debug("Script found: " + f.getName());
            if (f.getName().equals(name))
                return f;
        }
        throw new FileNotFoundException(new File(scriptsPath, name).getAbsolutePath());
    }

    public Class getScriptAsClass(String name) throws IOException {
        File file = getScript(name);
        GroovyClassLoader gcl = new GroovyClassLoader();
        return gcl.parseClass(file);
    }

    private class UpdateTask extends TimerTask {
        private File path;

        private UpdateTask(File path) {
            this.path = path;
        }

        @Override
        public void run() {
            if (path.isDirectory()) {
                removeOldScripts();
                addNewScripts();
            }
        }

        private void removeOldScripts() {
            Iterator<File> iterator = scripts.iterator();
            while (iterator.hasNext()) {
                File script = iterator.next();
                if (!script.isFile()) {
                    scripts.remove(script);
                    logger.info("Removed script: " + script.getName());
                }
            }
        }

        private void addNewScripts() {
            for (File f : path.listFiles()) {
                if (!scripts.contains(f) && isValidScriptFile(f)) {
                    scripts.add(f);
                    logger.info("Added script : " + f.getName() + " at " +
                                        path.getAbsolutePath());
                }
            }
        }

        private Boolean isValidScriptFile(File f) {
            return (f.getName().endsWith("groovy") || f.getName().endsWith("groovy-script"));
        }


    }


}
