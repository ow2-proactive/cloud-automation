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

        while (extractVmsData(args).getNroVmsBuilding() > 0) {
            logger.debug("Waiting for building to be ready...")
            Thread.sleep(1000 * 60);
        }

        logger.debug("Ready to stop all...")
        Vms allMetadataVms = extractVmsData(args)

        for (Vm vm: allMetadataVms.getVms(VmStatus.READY)) {
            logger.debug("Shutting down: " + vm.platform)
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

