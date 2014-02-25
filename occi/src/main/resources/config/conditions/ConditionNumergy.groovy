package config.conditions

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONObject
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance
import org.ow2.proactive.brokering.triggering.Condition
import org.ow2.proactive.brokering.triggering.utils.elasticity.ElasticityUtils

class ConditionNumergy implements Condition {

    private HttpClient httpClient

    public static void main(String[] a) throws Exception {
        new ConditionNumergy().evaluate(new HashMap<String, String>())
    }

    public ConditionNumergy() {
        httpClient = new DefaultHttpClient()
    }

    public boolean evaluate(Map<String, String> args) {

        println ">>>>>>>>>>>>> Condition checking... "

        def serverUrl = getAttribute("occi.server.endpoint", args)
        def platformBaseLocation = getAttribute("occi.paas.elasticity.base", args);

        ResourceInstance esPlatformBaseResource = getEsBaseResource(platformBaseLocation, serverUrl)

        String ipBase = getIpCheckValid(esPlatformBaseResource)

        def url = "http://" + ipBase + ":9200"
        JSONObject esIndices = getEsIndices(url)
        JSONObject esNodes = getEsNodes(url)

        ElasticityUtils.Vms vms = ElasticityUtils.extractVmsData(args)
        updateVmsStatus(esNodes, vms)
        ElasticityUtils.updataVmsData(args, vms);

        return scaleUpOrDown(esIndices, vms, esNodes)
    }

    private String getIpCheckValid(ResourceInstance esPlatformBaseResource) {
        def ipBase = esPlatformBaseResource.get("occi.networkinterface.address")

        if (ipBase == null || ipBase.isEmpty())
            throw new RuntimeException("IP of master not available yet...")

        return ipBase
    }

    private boolean scaleUpOrDown(JSONObject esIndices, ElasticityUtils.Vms vms, JSONObject esNodes) {
        def nroNodesBuilding = vms.getNroVmsBuilding()
        def nroNodesShuttingDown = vms.getNroVmsShuttingDown()
        def nroNodesBuildingExpired = vms.getNroVmsBuildingExpired()
        int missing = esIndices.size() - nroNodesBuilding - esNodes.size()

        println "   + indices: " + esIndices.size()
        println "   - building: " + nroNodesBuilding
        println "   - nodes: " + esNodes.size()
        println "   = missing: " + missing
        println ""
        println "   shutting down: " + nroNodesShuttingDown
        println "   building (expired): " + nroNodesBuildingExpired


        if (missing > 0) {
            println "   scale out -->>"
            return true;
        }

        if (missing < 0) {

            if (nroNodesBuilding != 0) {
                println "   should scale down, but unstable situation so no scaling"
                throw new RuntimeException("unstable situation (building in process), no scaling down")
            }

            println "   scale down -->>"
            return false;
        }

    }


    private ResourceInstance getEsBaseResource(String platformBaseLocation, String serverUrl) {
        def esBaseStub = new ResourceInstance(platformBaseLocation)
        def client = new OcciClient(serverUrl);
        return client.getResource("platform", esBaseStub.getUuid())
    }


    private void updateVmsStatus(JSONObject esNodes, ElasticityUtils.Vms vms) {
        for (String name : esNodes.keySet()) {
            JSONObject node = esNodes.get(name)
            String humanName = node.get("name")

            ElasticityUtils.Vm vm = vms.get(humanName)
            if (vm == null) {
                println "Vm $humanName not found!!!!"
            } else {
                vm.status = ElasticityUtils.VmStatus.READY
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

    private String getAttribute(String key, Map<String, String> args) {
        if (args.containsKey(key))
            return args.get(key)
        else
            throw new RuntimeException("$key argument not given...")
    }


}

