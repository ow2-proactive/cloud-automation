package config.actions

import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Action
import org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils

class ActionFalseNumergy implements Action {

    @Override
    void execute(Map<String, String> args) {

        println ">>>>>>>>>>>>> Action scaling down... "

        def serverUrl = getAttribute("occi.server.endpoint", args)


        ElasticityUtils.Vms vms = ElasticityUtils.extractVmsData(args)

        ElasticityUtils.Vm vm = vms.getOneReadyVm()

        println "Vm chosen: " + vm.platform

        ResourceInstance vmSlave = getResource(vm.platform, serverUrl)

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>"
        println ">>>>> Shutting down resource " + vmSlave
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>"
        shutdownVm(serverUrl, vmSlave)

        vm.status = ElasticityUtils.VmStatus.SHUTTINGDOWN
        ElasticityUtils.updataVmsData(args, vms)

    }

    private ResourceInstance shutdownVm(String serverUrl, ResourceInstance vmSlave) {
        OcciClient client = new OcciClient(serverUrl);
        return client.updateResource("platform", vmSlave.getUuid(), Collections.EMPTY_MAP, "stop");
    }

    private ElasticityUtils.Vm generateVmData(ResourceInstance instance) {
        ElasticityUtils.Vm vmData = new ElasticityUtils.Vm();
        vmData.status = ElasticityUtils.VmStatus.BUILDING
        vmData.started = new Date().getTime()
        vmData.platform = instance.getLocation()
        vmData.expiral = new Date().getTime() + 1000 * 60 * 60
        return vmData
    }

    private HashMap<String, String> generateVmParameters(String vmName, String ipBase) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("rule", "numergy");
        parameters.put("provider", "numergy");
        parameters.put("numergy.vm.name", vmName);
        parameters.put("paas.elasticsearch.master.ip", ipBase)
        return parameters
    }

    private ResourceInstance getResource(String platformBaseLocation, String serverUrl) {
        def platformBaseStub = new ResourceInstance(platformBaseLocation)
        OcciClient client = new OcciClient(serverUrl);
        def platformBase = client.getResource("platform", platformBaseStub.getUuid())
        return platformBase
    }


    private String getAttribute(String key, Map<String, String> args) {
        if (args.containsKey(key))
            return args.get(key)
         else
            throw new RuntimeException("$key argument not given...")
    }
}

