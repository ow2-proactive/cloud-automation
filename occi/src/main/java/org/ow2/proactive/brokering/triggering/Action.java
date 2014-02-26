package org.ow2.proactive.brokering.triggering;

import java.util.Map;

public abstract class Action {
    public abstract void execute(Map<String, String> attributes);

    protected String getAttribute(String key, Map<String, String> args) {
        if (args.containsKey(key))
            return args.get(key);
        else
            throw new RuntimeException(key + " argument not given...");
    }


}
