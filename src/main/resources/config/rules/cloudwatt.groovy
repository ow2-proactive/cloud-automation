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
        attributes.put("utils.scripts.server.address", "http://10.0.0.2:8000");
        attributes.put("iaas.provider.name", "NOVA");
        attributes.put("iaas.provider.api.url", "http://node4.cloud.sophia.inria.fr:5000/v2.0");
        attributes.put("iaas.provider.api.user", "admin");
        attributes.put("iaas.provider.api.password", "***REMOVED***");
        attributes.put("iaas.provider.api.tenant", "Admin");
        attributes.put("iaas.provider.newvm.image", "fd5c12dc-10e4-4a7f-b984-f221d848af07");
        attributes.put("iaas.provider.newvm.flavor", "2");
        attributes.put("iaas.provider.newvm.java.home.dir", "/home/ubuntu/opt/jdk/");
        attributes.put("iaas.provider.newvm.proactive.home.dir", "/home/ubuntu/opt/proactive/latest/scheduling/");
        attributes.put("proactive.rm.url", "pamr://0/");
        attributes.put("proactive.rm.credentials", "UlNBCjEwMjQKUlNBL0VDQi9QS0NTMVBhZGRpbmcKh7ucJr4s5kcMLNplKvoQMyq0JGlDQNbJTqmWuyTA2U9gJYACHZfTME6orv5837HUAYQjLLsASq3BV8O0Y1T/MoRyZ6hOGzFG26u4hrZHNNr+IxvHbA0BKeju2XW6kE/Tbjz2eFHrogCnrAJHE5G4hrp6B6AkVVZoqzIc5R+f/je6j1/ZK+f+4erc0FWSH6DcxGG1nHZeAyue/7digDw5+PbnZIg3w9yZVe/b7nfN0Ablh0B8DW4Tqqlvxhc+/RPQVwnXNw62M6NUzO9beQMY4EQFVhJc1FHBU+RhNVzku90gXkqPvi/nc1TE0qZli/OQhJQTWYqqhmBOjVX3eVm92FOWT2UWZVkh7ll2vSjxdPYqKI2jng1mw3etbYGlOUKPZtOuTuS1mAswh47k/wZBfQ==");
        attributes.put("proactive.rm.nodesource.name", "OPEN_STACK_MONITORING");
        attributes.put("utils.scripts.server.address", "http://10.0.0.2:8000");
        attributes.put("utils.scripts.rscripts.path", "/home/ubuntu/utils/rscript.sh");
        attributes.put("utils.script.relativepath", "general/show-info.sh");
        attributes.put("iaas.provider.newvm.name", "vmnamev002");

        return attributes;
    }
}