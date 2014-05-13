package org.ow2.proactive.brokering.occi.client.examples

import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance

// Instantiate an OCCI client for Cloud Automation API
OcciClient client = new OcciClient("http://try.activeeon.com/cloudautomation/");

// Create elastic platform
def argu = ["provider":"openstack","rule":"try","elasticity.vm.count.maximum":"2","elasticity.vm.count.minimum":"0","application":"elasticsearch","flavor":"elastic"]
def elasticPlatform = client.createResource("platform", argu);

// Wait so that the bitPlatform updates its attributes
while(!"done".equalsIgnoreCase(elasticPlatform.get("action.state"))) {
    elasticPlatform.refresh(client)
    Thread.sleep(1000*2)
}

// Get information about resources created
def masterLocation = elasticPlatform.get("occi.paas.elasticity.masterplatform")
def masterPlatform = new ResourceInstance(masterLocation).refresh(client)

def triggerLocation = elasticPlatform.get("occi.paas.elasticity.trigger")
def trigger = new ResourceInstance(triggerLocation).refresh(client)

// Show resources created
println "Elastic platform: " + elasticPlatform.toString()
println "Master platform: " + masterPlatform.toString()
println "Trigger platform: " + trigger.toString()

// Install kibana for elasticsearch in the master node
masterPlatform.update(client, [application:"kibana"], "install")

// Shutdown the whole platform
platformbig = client.updateResource(elasticPlatform, [:], "stop");



