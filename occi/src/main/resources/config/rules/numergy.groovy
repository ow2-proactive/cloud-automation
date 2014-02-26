package config.rules

import org.ow2.proactive.brokering.Rule

public class Numergy implements Rule {

    boolean match(Map<String, String> attributes) {
        String rule = attributes.get("rule");
        return (rule!=null && rule.contains("numergy"))
    }

    Map<String, String> apply(Map<String, String> attributes) {

        attributes.put("occi.server.endpoint", "http://10.200.96.143:8081/occi/api/occi")
        attributes.put("occi.compute.vendor.name", "NUMERGY")

        attributes.put("numergy.accesskey", "***REMOVED***")
        attributes.put("numergy.secretkey", "***REMOVED***") 
        attributes.put("numergy.tenantid", "***REMOVED***") 
        attributes.put("numergy.endpoint", "https://109.24.132.213/")
        attributes.put("numergy.metadataserver", "http://localhost:9200/") 
        attributes.put("numergy.vm.instanceref", "50335578-9971-11e3-8d40-005056992152")

        attributes.put("paas.elasticsearch.node.name", "masternode")


        attributes.put("proactive.rm.url", "pnp://10.200.96.143:64738/")
        attributes.put("proactive.protocol", "pnp")
        attributes.put("proactive.router_port", "")
        attributes.put("proactive.router_address", "")
        attributes.put("proactive.credentials", "***REMOVED***")
        attributes.put("proactive.node_source_name", "Default")

        return attributes;

    }

}