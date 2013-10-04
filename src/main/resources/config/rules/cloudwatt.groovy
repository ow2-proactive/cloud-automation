package config.rules

import org.ow2.proactive.brokering.Rule

public class Cloudwatt implements Rule {
    boolean match(Map<String, String> attributes) {
        return "cloudwatt".equalsIgnoreCase(attributes.get("rule"));
    }

    Map<String, String> apply(Map<String, String> attributes) {

        attributes.put("occi.compute.vendor.name", "OPENSTACK");
        attributes.put("endpoint", "***REMOVED***");
        attributes.put("iaas.provider.name", "NOVA");
        attributes.put("iaas.provider.api.url", "***REMOVED***");
        attributes.put("iaas.provider.api.user", "mjost");
        attributes.put("iaas.provider.api.password", "***REMOVED***");
        attributes.put("iaas.provider.api.tenant", "***REMOVED***");
        attributes.put("iaas.provider.newvm.image", "***REMOVED***");
        attributes.put("iaas.provider.newvm.flavor", "18");
        attributes.put("iaas.provider.newvm.java.home.dir", "/opt/jdk1.6.0_25/");
        attributes.put("iaas.provider.newvm.proactive.home.dir", "/opt/scheduling/");
        attributes.put("proactive.rm.url", "pnp://194.2.202.227:64738/");
        attributes.put("proactive.rm.credentials", "***REMOVED***");
        attributes.put("proactive.rm.nodesource.name", "OPEN_STACK_MONITORING");

        return attributes;

    }

}