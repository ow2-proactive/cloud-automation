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

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractDevice {

    public static final short STARDARD = 1;
    public static final short JLINE = 2;

    public abstract String readLine(String fmt, Object... args)
            throws IOException;

    public abstract char[] readPassword(String fmt, Object... args)
            throws IOException;

    public abstract void writeLine(String fmtm, Object... args)
            throws IOException;

    public abstract Writer getWriter();

    public static AbstractDevice getConsole(int type) throws IOException {
        switch (type) {
        case STARDARD:
            return (System.console() != null) ? new ConsoleDevice(
                    System.console()) : new CharacterDevice(System.in,
                    System.out);
        case JLINE:
            return new JLineDevice(System.in, System.out);
        default:
            throw new IllegalArgumentException("Unknown console type [" + type
                    + "]");
        }
    }
}
