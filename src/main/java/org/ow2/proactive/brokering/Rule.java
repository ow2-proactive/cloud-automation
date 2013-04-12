package org.ow2.proactive.brokering;

import java.util.Map;

public interface Rule {
    public boolean match(Attributes attributes);

    public Map<String, String> apply(Attributes attributes);
}
