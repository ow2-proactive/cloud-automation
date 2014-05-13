package config.conditions

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONObject
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.ow2.proactive.brokering.monitoring.MonitoringProxy
import org.ow2.proactive.brokering.monitoring.NodeMonitoring
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Condition
import static org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils.*

class ConditionES extends Condition {

    private static final Logger logger = Logger.getLogger(ConditionES.class.getName());

    private HttpClient httpClient

    public ConditionES() {
        httpClient = new DefaultHttpClient()
    }

    public Boolean evaluate(Map<String, String> args) {

        def restServerUrl = getAttribute("proactive.rest.url", args)
        def occiServerUrl = getAttribute("occi.server.endpoint", args)
        def credentials = getAttribute("proactive.rm.credentials", args)
        def platformBaseUrl = getAttribute("occi.paas.elasticity.masterplatform", args)
        def vmCountMaximum = Integer.parseInt(getAttribute("elasticity.vm.count.maximum", args))
        def vmCountMinimum = Integer.parseInt(getAttribute("elasticity.vm.count.minimum", args))

        ResourceInstance esPlatformBaseResource = getResource(platformBaseUrl, occiServerUrl)

        String nodeUrlBase = getNodeUrlCheckValid(esPlatformBaseResource)
        String nodeName = getNodeNameCheckValid(esPlatformBaseResource)
        if (nodeUrlBase == null) {
            logger.debug "SO: +0 (no master yet)"
            return null
        }

        MonitoringProxy proxy = createMonitoringProxy(restServerUrl, credentials, generateJmxUrl(nodeUrlBase, nodeName))

        JSONObject esIndices = getEsIndices(proxy)
        JSONObject esNodesAndMaster = getEsNodes(proxy)

        Vms vms = extractVmsData(args)
        updateVmsStatus(esNodesAndMaster, vms)
        updataVmsData(args, vms);

        return scaleUpOrDown(
                esIndices.size(),
                vms,
                esNodesAndMaster.size(),
                vmCountMaximum,
                vmCountMinimum)
    }

    private String getNodeUrlCheckValid(ResourceInstance esPlatformBaseResource) {
        def nodeUrl = esPlatformBaseResource.get("proactive.node.url")
        return (nodeUrl == null || nodeUrl.isEmpty() ? null : nodeUrl)
    }

    private String getNodeNameCheckValid(ResourceInstance esPlatformBaseResource) {
        def nodeName = esPlatformBaseResource.get("occi.compute.hostname")
        return (nodeName == null || nodeName.isEmpty() ? null : nodeName)
    }

    private Boolean scaleUpOrDown(
                    Integer nodesRequired,
                    Vms metadataVms,
                    Integer nodesUpAndMaster,
                    Integer maxNodes,
                    Integer minNodes
    ) {

        def nodesBuilding = metadataVms.getNroVmsBuilding()
        def nroNodesShuttingDown = metadataVms.getNroVmsShuttingDown()
        def nroNodesBuildingExpired = metadataVms.getNroVmsBuildingExpired()
        int nodesMissing = nodesRequired - nodesBuilding - (nodesUpAndMaster-1)

        logger.debug "   + indices: " + nodesRequired
        logger.debug "   - building: " + nodesBuilding
        logger.debug "   - nodes: " + (nodesUpAndMaster-1)
        logger.debug "   = missing: " + nodesMissing
        logger.debug ""
        logger.debug "   shutting down: " + nroNodesShuttingDown
        logger.debug "   building (expired): " + nroNodesBuildingExpired
        logger.debug "   max: " + maxNodes
        logger.debug "   min: " + minNodes

        int total = nodesBuilding + (nodesUpAndMaster-1)

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


    private MonitoringProxy createMonitoringProxy(String restUrl, String credentials, String jmxUrl) {
        MonitoringProxy proxy = new MonitoringProxy.Builder()
        //.setRestUrl("http://try.activeeon.com/rest/rest")
        .setRestUrl(restUrl)
        //.setCredentials("***REMOVED***")
        .setCredentials(credentials)
        .setInsecureAccess()
        //.setJmxUrl("service:jmx:ro:///jndi/pamr://4211/rmnode")
        .setJmxUrl(jmxUrl)
        .setType("PFlags")
        .build();
        return proxy;
    }

    private String generateJmxUrl(String nodeUrl, String nodeName) {
        return "service:jmx:ro:///jndi/" + nodeUrl.replace(nodeName, "rmnode");

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

    private JSONObject getEsNodes(MonitoringProxy proxy) {
        //String entityNodes = executeHttpGet(url, "/_nodes")
        NodeMonitoring node = new NodeMonitoring(proxy);
        Map<String, String> map = node.getMBeanAttributeAsMap("PFlags");
        String entityNodes = map.get("esnodes");
        return JsonPath.read(entityNodes, "\$.nodes")
    }

    private JSONObject getEsIndices(MonitoringProxy proxy) {
        //String entityStatus = executeHttpGet(url, "/_status")
        NodeMonitoring node = new NodeMonitoring(proxy);
        Map<String, String> map = node.getMBeanAttributeAsMap("PFlags");
        String entityStatus = map.get("esstatus");
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

