package config.rules

import org.ow2.proactive.brokering.Rule

public class Try implements Rule {
    boolean match(Map<String, String> attributes) {
        return "try".equalsIgnoreCase(attributes.get("rule"));
    }

    Map<String, String> apply(Map<String, String> attributes) {

        //  ***REMOVED***

        // About OpenStack
        attributes.put("iaas.provider.name", "openstack");
        attributes.put("iaas.provider.api.url", "http://100.90.6.21:5000/v2.0/"); // openstack-try local
        attributes.put("iaas.provider.api.user", "admin");
        attributes.put("iaas.provider.api.password", "***REMOVED***");
        attributes.put("iaas.provider.api.tenant", "admin");
        attributes.put("iaas.provider.vm.image", "***REMOVED***");
        attributes.put("iaas.provider.vm.flavor", "***REMOVED***");

        // About the VM image in OpenStack
        attributes.put("iaas.provider.vm.image.proactive.home", "/root/scheduling/");
        attributes.put("iaas.provider.vm.image.java.home", "/root/java-7-oracle/");

        // About the Scheduler
        attributes.put("proactive.rm.url", "pamr://0/");
        attributes.put("proactive.router.protocol", "pamr");
        attributes.put("proactive.router.address", "100.90.32.46"); // scheduling-try local
        attributes.put("proactive.router.port", "8090");
        attributes.put("proactive.rm.credentials", "***REMOVED***");

        attributes.put("proactive.rm.nodesource.name", "Default");

        attributes.put("occi.server.endpoint", "http://localhost:8081/occi/api/occi");

        return attributes;
    }
}