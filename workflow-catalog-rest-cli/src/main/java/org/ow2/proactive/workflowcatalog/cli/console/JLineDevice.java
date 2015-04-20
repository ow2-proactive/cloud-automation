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



package org.ow2.proactive.workflowcatalog.cli.console;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import jline.*;

public class JLineDevice extends AbstractDevice {
    private static final int HLENGTH = 100000;
    private static final String HFILE = System.getProperty("user.home")
            + File.separator + ".proactive" + File.separator + "wccli.hist";

    private ConsoleReader reader;
    private PrintWriter writer;
    private List<Completor> completors;

    public JLineDevice(InputStream in, PrintStream out) throws IOException {
        File hfile = new File(HFILE);
        if (!hfile.exists()) {
            File parentFile = hfile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            hfile.createNewFile();
        }
        writer = new PrintWriter(out, true);
        reader = new ConsoleReader(in, writer);
        completors = new ArrayList<Completor>();
        reader.setHistory(new History(hfile));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                writeHistory();
            }
        }));
    }

    public Writer getWriter() {
        return writer;
    }

    private void writeHistory() {
        FileOutputStream outStream = null;
        try {
            File hfile = new File(HFILE);
            if (hfile.exists()) {
                hfile.delete();
            }
            hfile.createNewFile();
            outStream = new FileOutputStream(hfile);
            PrintWriter decorated = new PrintWriter(outStream);
            @SuppressWarnings("rawtypes")
            List historyList = reader.getHistory().getHistoryList();
            if (historyList.size() > HLENGTH) {
                historyList = historyList.subList(historyList.size() - HLENGTH,
                        historyList.size());
            }
            for (int index = 0; index < historyList.size(); index++) {
                decorated.println(historyList.get(index));
            }
            decorated.flush();
        } catch (IOException fnfe) {
            // can't do much 
            
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void addAutocompleteCommand(Completor completor) {
        for (Object c: reader.getCompletors()) {
            reader.removeCompletor((Completor)c);
        }
        completors.add(completor);
        reader.addCompletor(new MultiCompletor(completors));
    }

    @Override
    public String readLine(String fmt, Object... args) throws IOException {
        return reader.readLine(String.format(fmt, args));
    }

    @Override
    public char[] readPassword(String fmt, Object... args) throws IOException {
        // String.format(fmt, args),
        return reader.readLine(String.format(fmt, args), new Character('*'))
                .toCharArray();

    }

    @Override
    public void writeLine(String format, Object... args) throws IOException {
        reader.printString(String.format(format, args));
        reader.printNewline();
    }

}
