package org.ow2.proactive.brokering.triggering;

import java.util.Map;

public interface Condition {

    public boolean evaluate(Map<String, String> args);

}
