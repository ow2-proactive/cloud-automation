package config.rules

import org.ow2.proactive.brokering.Attributes
import org.ow2.proactive.brokering.Rule

public class Vcloud implements Rule {
    boolean match(Attributes attributes) {
        return (attributes.get("sla").equalsIgnoreCase("gold") || attributes.get("sla").equalsIgnoreCase("silver"));
    }

    Map<String, String> apply(Attributes attributes) {
        attributes.put("occi.compute.vendor.name", "VCLOUD");
        attributes.put("endpoint", "https://vcd.cloud.univcloud.fr");
        attributes.put("login", "***REMOVED***");
        attributes.put("password", "***REMOVED***");

        String location = attributes.get("occi.compute.vendor.location");
        String sla = attributes.get("sla");
        attributes.put("occi.compute.vendor.location", location + "-" + sla.capitalize())
    }
}