package config.rules

import org.ow2.proactive.brokering.Rule

public class OpenStack implements Rule {
    boolean match(Map<String, String> attributes) {
        return false; //(attributes.get("sla").equalsIgnoreCase("bronze"));
    }

    Map<String, String> apply(Map<String, String> attributes) {
        attributes.put("occi.compute.vendor.name", "OPENSTACK");
        attributes.put("endpoint", "***REMOVED***");
        attributes.put("login", "admin");
        attributes.put("password", "activeeon");
        attributes.put("occi_endpoint", "http://127.0.0.1:8183/brokering/api/occi/");

        if (attributes.get("sla") != null && attributes.get("occi.core.location") != null) {
            String sla = attributes.get("sla").capitalize();
            String location = attributes.get("occi.core.location").toUpperCase();
            attributes.put("occi.compute.vendor.location", location + "-" + sla);
        }

        return attributes;
    }
}