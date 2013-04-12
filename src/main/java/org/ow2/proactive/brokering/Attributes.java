package org.ow2.proactive.brokering;

import java.util.HashMap;
import java.util.Set;

public class Attributes {
    private HashMap<String, String> attributes;

    public Attributes() {
        attributes = new HashMap<String, String>();
    }

    public Attributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public Attributes(String attrs) {
        this();
        for(String entry : attrs.split(",")) {
            String[] keyval = entry.split("=>");
            attributes.put(keyval[0].trim(), keyval[1].trim());
        }
    }

    public void put(String key, String value) {
        attributes.put(key, value);
    }

    public String get(String key) {
        return attributes.get(key);
    }

    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public String toString() {
        String res = "Attributes = {";
        for (String key : attributes.keySet()) {
            res += key + ":" + attributes.get(key) + ",";
        }
        return res;
    }
}
