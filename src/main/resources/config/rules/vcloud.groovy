package config.rules

import org.ow2.proactive.brokering.Rule

public class Vcloud implements Rule {
    boolean match(Map<String, String> attributes) {
        return true; //(attributes.get("sla").contains("gold") || attributes.get("sla").contains("silver"));
    }

    Map<String, String> apply(Map<String, String> attributes) {
        attributes.put("occi.compute.vendor.name", "vcloud");
        attributes.put("endpoint", "https://vcd.cloud.univcloud.fr");
//        attributes.put("endpoint", "https://127.0.0.1:8443");
        attributes.put("login", "***REMOVED***");
        attributes.put("password", "***REMOVED***");
        attributes.put("occi_endpoint", "***REMOVED***");

        if (attributes.get("sla") != null && attributes.get("occi.core.location") != null) {
            String sla = attributes.get("sla").capitalize();
            String location = attributes.get("occi.core.location").toUpperCase();
            attributes.put("occi.compute.vendor.location", location + "-" + sla);
        }

        return attributes;
    }
}