package config.actions

import org.apache.log4j.Logger
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Action
import static org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils.*

class ActionTrueNumergy extends Action {

    private static final Logger logger = Logger.getLogger(ActionTrueNumergy.class.getName());

    @Override
    void execute(Map<String, String> args) {

        def serverUrl = getAttribute("occi.server.endpoint", args)
        def platformBaseLocation = getAttribute("occi.paas.elasticity.base", args);

        ResourceInstance esPlatformBaseResource = getPlatformBaseResource(platformBaseLocation, serverUrl)

        String ipBase = getIpCheckValid(esPlatformBaseResource)

        String vmName = generateSlaveVmName()
        HashMap<String, String> parameters = generateVmParameters(vmName, ipBase)

        OcciClient client = new OcciClient(serverUrl);
        def instance = client.createResource("platform", parameters, null)

        logger.info("Creating resource: " + parameters)

        Vms vms = extractVmsData(args)
        vms.put(vmName, generateVmData(instance))
        updataVmsData(args, vms)
    }

    private Vm generateVmData(ResourceInstance instance) {
        Vm vmData = new Vm();
        vmData.status = VmStatus.BUILDING
        vmData.started = new Date().getTime()
        vmData.platform = instance.getLocation()
        vmData.expiral = new Date().getTime() + 1000 * 60 * 60
        return vmData
    }

    private HashMap<String, String> generateVmParameters(String vmName, String ipBase) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("rule", "numergy");
        parameters.put("provider", "numergy");
        parameters.put("application", "elasticsearch");
        parameters.put("flavor", "single");
        parameters.put("numergy.vm.name", vmName);
        parameters.put("paas.elasticsearch.master.ip", ipBase)
        return parameters
    }

    private String generateSlaveVmName() {
        Random rnd = new Random();
        String vmName = "Slave-" + Math.abs(rnd.nextInt());
        return vmName
    }

    private String getIpCheckValid(ResourceInstance esPlatformBaseResource) {
        def ipBase = esPlatformBaseResource.get("occi.networkinterface.address")

        if (ipBase == null || ipBase.isEmpty())
            throw new RuntimeException("Master not ready")

        return ipBase
    }

    private ResourceInstance getPlatformBaseResource(String platformBaseLocation, String serverUrl) {
        def platformBaseStub = new ResourceInstance(platformBaseLocation)
        OcciClient client = new OcciClient(serverUrl);
        def platformBase = client.getResource("platform", platformBaseStub.getUuid())
        return platformBase
    }

}

