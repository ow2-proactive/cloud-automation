package org.ow2.proactive.brokering.triggering;

import java.util.Map;

public abstract class Condition {

    public abstract Boolean evaluate(Map<String, String> args);

    protected String getAttribute(String key, Map<String, String> args) {
        if (args.containsKey(key))
            return args.get(key);
        else
            throw new RuntimeException(key + " argument not given...");
    }

}
