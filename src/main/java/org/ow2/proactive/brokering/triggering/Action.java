package org.ow2.proactive.brokering.triggering;

import java.util.Map;

public interface Action {
    public void execute(Map<String, String> attributes);
}
