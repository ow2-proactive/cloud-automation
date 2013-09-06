package config.rules

import org.ow2.proactive.brokering.Rule

public class Cloudwatt implements Rule {
    boolean match(Map<String, String> attributes) {
        return "cloudwatt".equalsIgnoreCase(attributes.get("rule"));
    }

    Map<String, String> apply(Map<String, String> attributes) {
        attributes.put("occi.compute.vendor.name", "OPENSTACK");
        attributes.put("endpoint", "***REMOVED***");
        attributes.put("login", "admin");
        attributes.put("password", "activeeon");
        attributes.put("occi_endpoint", "***REMOVED***");
        return attributes;
    }
}