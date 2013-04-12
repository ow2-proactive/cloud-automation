package config.rules

import org.ow2.proactive.brokering.Attributes
import org.ow2.proactive.brokering.Rule

public class OpenStack implements Rule {
    boolean match(Attributes attributes) {
        return (attributes.get("sla").equalsIgnoreCase("bronze"));
    }

    Map<String, String> apply(Attributes attributes) {
        attributes.put("occi.compute.vendor.name", "OPENSTACK");
        attributes.put("endpoint", "***REMOVED***");
        attributes.put("login", "admin");
        attributes.put("password", "activeeon");

        String location = attributes.get("occi.compute.vendor.location");
        String sla = attributes.get("sla");
        attributes.put("occi.compute.vendor.location", location + "-" + sla.capitalize())
    }
}