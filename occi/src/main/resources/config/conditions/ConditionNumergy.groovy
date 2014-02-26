package config.conditions

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONObject
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Condition
import static org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils.*

class ConditionNumergy extends Condition {

    private static final Logger logger = Logger.getLogger(ConditionNumergy.class.getName());

    private HttpClient httpClient

    public ConditionNumergy() {
        httpClient = new DefaultHttpClient()
    }

    public Boolean evaluate(Map<String, String> args) {

        def occiServerUrl = getAttribute("occi.server.endpoint", args)
        def platformBaseUrl = getAttribute("occi.paas.elasticity.base", args)
        def vmCountMaximum = Integer.parseInt(getAttribute("elasticity.vm.count.maximum", args))
        def vmCountMinimum = Integer.parseInt(getAttribute("elasticity.vm.count.minimum", args))

        ResourceInstance esPlatformBaseResource = getResource(platformBaseUrl, occiServerUrl)

        String ipBase = getIpCheckValid(esPlatformBaseResource)
        if (ipBase == null) {
            logger.debug "SO: +0 (no master yet)"
            return null
        }

        def url = "http://" + ipBase + ":9200"
        JSONObject esIndices = getEsIndices(url)
        JSONObject esNodesAndMaster = getEsNodes(url)

        Vms vms = extractVmsData(args)
        updateVmsStatus(esNodesAndMaster, vms)
        updataVmsData(args, vms);

        return scaleUpOrDown(esIndices, vms, esNodesAndMaster, vmCountMaximum, vmCountMinimum)
    }

    private String getIpCheckValid(ResourceInstance esPlatformBaseResource) {
        def ipBase = esPlatformBaseResource.get("occi.networkinterface.address")
        return (ipBase == null || ipBase.isEmpty() ? null : ipBase)
    }

    private Boolean scaleUpOrDown(
                    JSONObject nodesRequired,
                    Vms metadataVms,
                    JSONObject nodesUpAndMaster,
                    Integer maxNodes,
                    Integer minNodes
    ) {

        def nodesBuilding = metadataVms.getNroVmsBuilding()
        def nroNodesShuttingDown = metadataVms.getNroVmsShuttingDown()
        def nroNodesBuildingExpired = metadataVms.getNroVmsBuildingExpired()
        int nodesMissing = nodesRequired.size() - nodesBuilding - (nodesUpAndMaster.size()-1)

        logger.debug "   + indices: " + nodesRequired.size()
        logger.debug "   - building: " + nodesBuilding
        logger.debug "   - nodes: " + (nodesUpAndMaster.size()-1)
        logger.debug "   = missing: " + nodesMissing
        logger.debug ""
        logger.debug "   shutting down: " + nroNodesShuttingDown
        logger.debug "   building (expired): " + nroNodesBuildingExpired

        int total = nodesBuilding + (nodesUpAndMaster.size()-1)

        if (total < minNodes) {
            logger.debug "SO: +1 (less than min)"
            return true
        }

        if (nodesMissing > 0) {

            if (total >= maxNodes) {
                logger.debug "SO: +0 (missing but max)"
                return null
            } else {
                logger.debug "SO: +1 (missing and less than max)"
                return true
            }

        } else  if (nodesMissing < 0) {

            if (nodesBuilding != 0) {
                logger.debug "SO: +0 (missing but some building)"
                return null
            }

            if (total > minNodes) {
                logger.debug "SO: -1 (missing no building, tot>min)"
                return false
            } else {
                logger.debug "SO: +0 (missing and no building, tot<=min)"
                return null
            }

        } else {

            logger.debug "SO: +0 (all right)"
            return null

        }

    }


    private ResourceInstance getResource(String platformBaseLocation, String serverUrl) {
        def esBaseStub = new ResourceInstance(platformBaseLocation)
        def client = new OcciClient(serverUrl);
        return client.getResource("platform", esBaseStub.getUuid())
    }


    private void updateVmsStatus(JSONObject esNodes, Vms vms) {
        for (String name : esNodes.keySet()) {
            JSONObject node = esNodes.get(name)
            String humanName = node.get("name")

            Vm vm = vms.get(humanName)
            if (vm == null) {
                logger.debug "VM $humanName not found in metadata"
            } else {
                vm.status = VmStatus.READY
            }

        }
    }

    private JSONObject getEsNodes(String url) {
        String entityNodes = executeHttpGet(url, "/_nodes")
        return JsonPath.read(entityNodes, "\$.nodes")
    }

    private JSONObject getEsIndices(String url) {
        String entityStatus = executeHttpGet(url, "/_status")
        return JsonPath.read(entityStatus, "\$.indices")
    }

    private String executeHttpGet(String url, String path) {
        HttpResponse responseStatus = httpClient.execute(new HttpGet(url + path));
        def code = responseStatus.getStatusLine().getStatusCode()

        if (code < 200 || code > 299)
            throw new RuntimeException("Could not query ES: " + responseStatus.getStatusLine())

        return EntityUtils.toString(responseStatus.getEntity())
    }

}

