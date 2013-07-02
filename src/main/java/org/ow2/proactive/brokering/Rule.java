package org.ow2.proactive.brokering;

import java.util.Map;

public interface Rule {
    public boolean match(Map<String, String> attributes);

    public Map<String, String> apply(Map<String, String> attributes);
}
