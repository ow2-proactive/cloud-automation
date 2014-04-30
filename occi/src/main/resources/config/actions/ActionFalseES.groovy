package config.actions

import org.apache.log4j.Logger
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Action
import static org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils.*

class ActionFalseES extends Action {

    private static final Logger logger = Logger.getLogger(ActionFalseES.class.getName());

    @Override
    void execute(Map<String, String> args) {
        def occiServerEndpoint = getAttribute("occi.server.endpoint", args)

        logger.debug("Scaling down")

        Vms allMetadataVms = extractVmsData(args)
        Vm vm = allMetadataVms.getOneRandomReadyVm()

        logger.debug("Victim: " + vm.platform)
        shutdownVm(occiServerEndpoint, vm.platform)
        vm.status = VmStatus.SHUTTINGDOWN

        updataVmsData(args, allMetadataVms)

    }

    private ResourceInstance shutdownVm(String serverUrl, String vmLocation) {
        OcciClient client = new OcciClient(serverUrl);
        ResourceInstance vmSlave = new ResourceInstance(vmLocation)
        return client.updateResource(vmSlave.getCategory(), vmSlave.getUuid(), Collections.EMPTY_MAP, "stop");
    }

}

