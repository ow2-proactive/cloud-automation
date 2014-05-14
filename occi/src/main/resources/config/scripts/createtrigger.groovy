import org.ow2.proactive.brokering.occi.client.OcciClient
import com.jayway.jsonpath.JsonPath

String prevresult = results[0].toString()
net.minidev.json.JSONObject json1 = JsonPath.parse(prevresult).json()
def basePlatform = json1.get("occi.paas.elasticity.masterplatform")
println "Platform of base: $basePlatform"

def occiServerUrl = "${occi.server.endpoint}"
def client = new OcciClient(occiServerUrl);

Map<String, String> ar = new HashMap<String, String>();
ar.put("rule","try");
ar.put("action.state", "done");
ar.put("occi.monitoring.periodms", "120000");
ar.put("occi.monitoring.condition", "ConditionES.groovy")
ar.put("occi.monitoring.trueaction", "ActionTrueES.groovy")
ar.put("occi.monitoring.falseaction", "ActionFalseES.groovy")
ar.put("occi.monitoring.stopaction", "ActionStopES.groovy")
ar.put("elasticity.vm.count.maximum", "${elasticity.vm.count.maximum}")
ar.put("elasticity.vm.count.minimum", "${elasticity.vm.count.minimum}")
ar.put("occi.paas.elasticity.masterplatform", basePlatform)
ar.put("occi.server.endpoint", occiServerUrl)

def resource = client.createResource("actiontrigger", ar);

def json = new net.minidev.json.JSONObject();
json.put("occi.paas.elasticity.trigger", resource.getLocation());
json.put("action.state", "done");
result = json.toJSONString()
println result

