package config.actions

import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Action
import org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils

class ActionTrueNumergy implements Action {

    @Override
    void execute(Map<String, String> args) {

        println ">>>>>>>>>>>>> Action scaling out... "

        def serverUrl = getAttribute("occi.server.endpoint", args)
        def platformBaseLocation = getAttribute("occi.paas.elasticity.base", args);

        ResourceInstance esPlatformBaseResource = getPlatformBaseResource(platformBaseLocation, serverUrl)

        String ipBase = getIpCheckValid(esPlatformBaseResource)

        String vmName = generateSlaveVmName()
        HashMap<String, String> parameters = generateVmParameters(vmName, ipBase)

        OcciClient client = new OcciClient(serverUrl);
        def instance = client.createResource("platform", parameters, null)
        //def instance = new ResourceInstance("http://localhost:8081/occi/api/occi/platform/a722403e-9b33-4cdf-82e6-ab6aa6a6da7f")
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>"
        println ">>>>> Creating resource with parameters: " + parameters
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>"

        ElasticityUtils.Vms vms = ElasticityUtils.extractVmsData(args)
        vms.put(vmName, generateVmData(instance))
        ElasticityUtils.updataVmsData(args, vms)
    }

    private ElasticityUtils.Vm generateVmData(ResourceInstance instance) {
        ElasticityUtils.Vm vmData = new ElasticityUtils.Vm();
        vmData.status = ElasticityUtils.VmStatus.BUILDING
        vmData.started = new Date().getTime()
        vmData.platform = instance.getLocation()
        vmData.expiral = new Date().getTime() + 1000 * 60 * 60
        vmData
    }

    private HashMap<String, String> generateVmParameters(String vmName, String ipBase) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("rule", "numergy");
        parameters.put("provider", "numergy");
        parameters.put("numergy.vm.name", vmName);
        parameters.put("paas.elasticsearch.master.ip", ipBase)
        parameters
    }

    private String generateSlaveVmName() {
        Random rnd = new Random();
        String vmName = "Slave-" + Math.abs(rnd.nextInt());
        vmName
    }

    private String getIpCheckValid(ResourceInstance esPlatformBaseResource) {
        def ipBase = esPlatformBaseResource.get("occi.networkinterface.address")

        if (ipBase == null || ipBase.isEmpty())
            throw new RuntimeException("IP of master not available yet...")

        return ipBase
    }

    private ResourceInstance getPlatformBaseResource(String platformBaseLocation, String serverUrl) {
        def platformBaseStub = new ResourceInstance(platformBaseLocation)
        OcciClient client = new OcciClient(serverUrl);
        def platformBase = client.getResource("platform", platformBaseStub.getUuid())
        platformBase
    }


    private String getAttribute(String key, Map<String, String> args) {
        if (args.containsKey(key))
            return args.get(key)
         else
            throw new RuntimeException("$key argument not given...")
    }
}

