
import org.ow2.proactive.iaas.*
import org.ow2.proactive.iaas.nova.*
import net.minidev.json.JSONObject

String accessKey = '${iaas.provider.api.user}'
String secretKey = '${iaas.provider.api.password}'
String tenantId = '${iaas.provider.api.tenant}'
String uri = '${iaas.provider.api.url}'

String instanceId = '${occi.compute.vendor.uuid}'

if (instanceId == null || instanceId.isEmpty())
    throw new RuntimeException("No VM uuid specified.");

println ">>> Logging to IaaS server..."

def api = NovaAPI.getNovaAPI(
        accessKey, secretKey, tenantId,
        new URI(uri));

println ">>> Undeploying VM: " + instanceId

api.stopInstance(new IaasInstance(instanceId))

println ">>> VM undeployed"

def json = new net.minidev.json.JSONObject()
json.put("occi.compute.state", "down")
result = json.toJSONString()
println result

