import org.ow2.proactive.iaas.*
import org.ow2.proactive.iaas.nova.*
import net.minidev.json.JSONObject

println "Current category ID: ${occi.core.id}"

String accessKey = '${iaas.provider.api.user}'
String secretKey = '${iaas.provider.api.password}'
String tenantId = '${iaas.provider.api.tenant}'
String uri = '${iaas.provider.api.url}'
String image = '${iaas.provider.vm.image}'
String flavor = '${iaas.provider.vm.flavor}'

String proactiveHome = '${iaas.provider.vm.image.proactive.home}'
String javaHome = '${iaas.provider.vm.image.java.home}'

String name = '${occi.compute.hostname}'

String rm_url='${proactive.rm.url}'
String protocol='${proactive.router.protocol}'
String router_port='${proactive.router.port}'
String router_address='${proactive.router.address}'
String credentials='${proactive.rm.credentials}'
String node_source_name='${proactive.rm.nodesource.name}'
String node_name = name
String token = name

String userdata =
    "#!/bin/bash\n" +
    "set -x\n" +
    "export LOGS=/tmp/rm-start-node.log\n" +
    "export JAVA_HOME="+javaHome+"\n" +
    "export CMD=\"$proactiveHome/bin/unix/rm-start-node -Dproactive.useIPaddress=true -Dsigar.pflag.path=/tmp/pflags -Dproactive.net.nolocal=true -Dproactive.node.access.token=$token -Dproactive.agent.rank=0 -Dproactive.communication.protocol=$protocol -Dproactive.pamr.router.address=$router_address -Dproactive.pamr.router.port=$router_port -r $rm_url -s $node_source_name -n $node_name -v $credentials \"\n" +
    "echo \$CMD > \$LOGS\n" +
    "date >> \$LOGS\n" +
    "\$CMD &>> \$LOGS\n"

println ">>> Logging to OpenStack..."

def api = NovaAPI.getNovaAPI(
        accessKey, secretKey, tenantId,
        new URI(uri));

def map = new HashMap<String, String>();
map.put(NovaAPI.NovaAPIConstants.InstanceParameters.USER_DATA, userdata);
map.put(NovaAPI.NovaAPIConstants.InstanceParameters.FLAVOR_REF, flavor);
map.put(NovaAPI.NovaAPIConstants.InstanceParameters.IMAGE_REF, image);
map.put(NovaAPI.NovaAPIConstants.InstanceParameters.NAME, name);

println ">>> Deploying VM..."

def instance = api.startInstance(map)

println ">>> VM deployed: " + instance.instanceId

def counter = 0
def ATTEMPTS = 15
while (!api.isInstanceStarted(instance)) {
    println ">>> Waiting for instance to be started..."
    Thread.sleep(1000 * 60 * 1)
    if (counter++ > ATTEMPTS)
        throw new RuntimeException("VM won't execute")
}

println ">>> VM started correctly."

result = instance.instanceId

def json = new net.minidev.json.JSONObject();
json.put("occi.compute.vendor.uuid", instance.instanceId)
json.put("occi.compute.state", "up")
result = json.toJSONString()
println result

