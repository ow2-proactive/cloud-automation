package org.ow2.proactive.brokering.triggering;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.ow2.proactive.brokering.utils.HttpUtility;

import java.util.Map;

public class ScriptUtils {

    public static Class getEncodedScriptAsClass(Map<String, String> args, String key)
            throws ScriptException {

        GroovyClassLoader gcl = new GroovyClassLoader();
        String argumentScript = args.get(key);

        if (argumentScript == null)
            return null;

        if (isClassName(argumentScript))
            try {
                return Class.forName(argumentScript);
            } catch (ClassNotFoundException e) {
                throw new ScriptException(e);
            }
        else if (encodedScriptIsNotEmpty(argumentScript))
            try {
                return gcl.parseClass(HttpUtility.decodeBase64(argumentScript));
            } catch (GroovyRuntimeException e) {
                throw new ScriptException(e);
            }

        throw new ScriptException("Cannot interpret: " + argumentScript);

    }

    private static boolean encodedScriptIsNotEmpty(String encodedScript) {
        return (!encodedScript.isEmpty());
    }

    private static boolean isClassName(String className) {
        return (className.contains("."));
    }

}


