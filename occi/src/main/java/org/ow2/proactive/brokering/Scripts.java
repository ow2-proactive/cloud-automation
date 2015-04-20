/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


package org.ow2.proactive.brokering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;

public class Scripts {

    protected static Logger logger = Logger.getLogger(Scripts.class.getName());
    protected final List<File> scripts;
    protected final File scriptsPath;

    public Scripts(File path, long refreshMs) {
        scripts = new LinkedList<File>();
        scriptsPath = path;

        Timer timer = new Timer();
        UpdateTask updateTask = new UpdateTask(scriptsPath);
        updateTask.run();
        timer.schedule(updateTask, refreshMs, refreshMs);
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
