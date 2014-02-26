package config.actions

import org.apache.log4j.Logger
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Action

import static org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils.*

class ActionStopNumergy extends Action {

    private static final Logger logger = Logger.getLogger(ActionStopNumergy.class.getName());

    @Override
    void execute(Map<String, String> args) {
        def occiServerEndpoint = getAttribute("occi.server.endpoint", args)

        logger.debug("Stopping all")

        if (extractVmsData(args).getNroVmsBuilding() > 0) {
            logger.warn("Some VMs are building, they will have to be deleted manually.")
            logger.warn(extractVmsData(args).getVms(VmStatus.BUILDING));
        }

        logger.debug("Ready to stop all...")
        Vms allMetadataVms = extractVmsData(args)

        Map<String, Vm> vms = allMetadataVms.getVms(VmStatus.READY)
        for (String vmName: vms.keySet()) {
            Vm vm = vms.get(vmName)
            logger.debug("Shutting down: " + vmName + ":" + vm.platform)
            shutdownVm(occiServerEndpoint, vm.platform)
            vm.status = VmStatus.SHUTTINGDOWN
        }
        updataVmsData(args, allMetadataVms)
        logger.debug("Done.")

    }

    private ResourceInstance shutdownVm(String serverUrl, String vmLocation) {
        OcciClient client = new OcciClient(serverUrl);
        ResourceInstance vmSlave = new ResourceInstance(vmLocation)
        return client.updateResource(vmSlave.getCategory(), vmSlave.getUuid(), Collections.EMPTY_MAP, "stop");
    }

}

