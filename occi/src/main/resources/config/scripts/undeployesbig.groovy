import org.ow2.proactive.brokering.occi.client.*

println "Current category ID: ${occi.core.id}"

def occiServerUrl = "${occi.server.endpoint}"
def client = new org.ow2.proactive.brokering.occi.client.OcciClient(occiServerUrl);

def base = new ResourceInstance("${occi.paas.elasticity.masterplatform}")
println "Base: " + base.getLocation()
def trigger = new ResourceInstance("${occi.paas.elasticity.trigger}")
println "Trigger: " + trigger.getLocation()

ResourceInstance base1 = client.updateResource(base.getCategory(), base.getUuid(), Collections.EMPTY_MAP, "stop");
ResourceInstance trigger1 = client.updateResource(trigger.getCategory(), trigger.getUuid(), Collections.EMPTY_MAP, "stop");

def json = new net.minidev.json.JSONObject();
json.put("occi.paas.state", "down");
result = json.toJSONString()
println result

