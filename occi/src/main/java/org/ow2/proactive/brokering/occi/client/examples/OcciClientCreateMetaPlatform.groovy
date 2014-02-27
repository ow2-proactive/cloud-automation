package org.ow2.proactive.brokering.occi.client.examples

import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance

OcciClient client = new OcciClient("http://localhost:8081/occi/api/occi");

def bigPlatform = new ResourceInstance("http://localhost:8081/occi/api/occi/platform/6675a9e6-9136-4c04-b343-cc7865342009").updateDownstream(client)
//def bigPlatform = null
if (bigPlatform == null) {
    //Map<String, String> ar = new HashMap<String, String>();
    def args = new HashMap<String, String>()
    args.put("provider", "numergy")
    args.put("rule", "numergy")
    args.put("elasticity.vm.count.maximum", "3")
    args.put("elasticity.vm.count.minimum", "2")
    args.put("application", "elasticsearch")
    args.put("flavor", "elastic")
    bigPlatform = client.createResource("platform", args);
    println "Big platform: " + bigPlatform.getLocation()
}

Thread.sleep(1000*10) // Wait so that the bitPlatform updates its attributes

bigPlatform.updateDownstream(client)

def masterLocation = bigPlatform.get("occi.paas.elasticity.masterplatform")
def triggerLocation = bigPlatform.get("occi.paas.elasticity.trigger")


def masterPlatform = new ResourceInstance(masterLocation).updateDownstream(client)
def trigger = new ResourceInstance(triggerLocation).updateDownstream(client)

println "Big platform: " + bigPlatform.toString()
println "Master platform: " + masterPlatform.toString()
println "Trigger platform: " + trigger.toString()

masterPlatform.updateUpstream(client, [application:"kibana"], "install")

platformbig = client.updateResource(bigPlatform.getCategory(), bigPlatform.getUuid(), Collections.EMPTY_MAP, "stop");



