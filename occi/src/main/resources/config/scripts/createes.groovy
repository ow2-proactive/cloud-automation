
import org.ow2.proactive.brokering.occi.client.OcciClient

println "Current category ID: ${occi.core.id}"

def vmName = "EsMasterE-" + new Random().nextInt(10000)
def occiServerUrl = "${occi.server.endpoint}"
def client = new OcciClient(occiServerUrl);
Map<String, String> ar = new HashMap<String, String>();
ar.put("rule","try");
ar.put("provider","openstack");
ar.put("occi.compute.hostname", vmName);
ar.put("flavor", "single");
ar.put("application", "elasticsearch");

def resource = client.createResource("platform", ar);

def json = new net.minidev.json.JSONObject();
json.put("occi.paas.application.endpoints", resource.getLocation())
json.put("occi.paas.elasticity.masterplatform", resource.getLocation());
result = json.toJSONString()
println result

