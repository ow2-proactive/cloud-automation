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


package org.ow2.proactive.brokering.triggering;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Scripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class ScriptUtils {

    private static final Logger logger = Logger.getLogger(ScriptUtils.class.getName());

    public static Class getEncodedScriptAsClass(Map<String, String> args, Scripts scripts, String key)
            throws ScriptException {

        GroovyClassLoader gcl = new GroovyClassLoader();
        String argumentScript = args.get(key);

        if (argumentScript == null)
            return null;

        if (isFileName(argumentScript))
            try {
                return scripts.getScriptAsClass(argumentScript);
            } catch (IOException e) {
                throw new ScriptException(e);
            }
        else if (encodedScriptIsNotEmpty(argumentScript))
            try {
                String script = decode(argumentScript);
                logger.debug("Script decoded: '" + script + "'");
                return gcl.parseClass(script);
            } catch (GroovyRuntimeException e) {
                throw new ScriptException(e);
            }

        throw new ScriptException("Cannot interpret: " + argumentScript);

    }

    private static boolean encodedScriptIsNotEmpty(String encodedScript) {
        return (!encodedScript.isEmpty());
    }

    private static boolean isFileName(String className) {
        return (className.contains("."));
    }

    public static String encode(String src) {
        return new String(Hex.encodeHex(src.getBytes()));
    }

    public static String decode(String src) {
        try {
            return new String(Hex.decodeHex(src.toCharArray()));
        } catch (DecoderException e) {
            throw new RuntimeException("Error decoding: " + src, e);
        }
    }

}


