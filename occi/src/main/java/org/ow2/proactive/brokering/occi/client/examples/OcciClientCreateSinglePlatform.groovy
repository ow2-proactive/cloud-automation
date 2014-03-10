package org.ow2.proactive.brokering.occi.client.examples


import org.ow2.proactive.brokering.occi.client.OcciClient

// Instantiate an OCCI client for Cloud Automation API
OcciClient client = new OcciClient("https://CA-SERVER/ca/");

// Create a static Elasticsearch master platform
Map<String, String> ar = new HashMap<String, String>();
ar.put("rule","numergy");
ar.put("provider","numergy");
ar.put("occi.compute.hostname", "EsMasterS-" + new Random().nextInt(10000));
ar.put("flavor", "single");
ar.put("application", "elasticsearch");
def resourceMaster = client.createResource("platform", ar);

// Wait so that the resourceMaster updates its attributes
while(!"done".equalsIgnoreCase(resourceMaster.get("action.state"))) {
    resourceMaster.refresh(client)
    Thread.sleep(1000*2)
}

println "Master: " + resourceMaster.toString()

// Create a static Elasticsearch slave platform
ar.put("occi.compute.hostname", "EsSlaveS-" + new Random().nextInt(10000));
ar.put("paas.elasticsearch.master.ip", resourceMaster.get("occi.networkinterface.address"))
def resourceClient = client.createResource("platform", ar);

// Wait so that the resourceMaster updates its attributes
while(!"done".equalsIgnoreCase(resourceClient.get("action.state"))) {
    resourceClient.refresh(client)
    Thread.sleep(1000*2)
}

println "Client: " + resourceClient.toString()

Thread.sleep(1000 * 60)

// Stop both static platforms
resourceMaster.update(client, Collections.EMPTY_MAP, "stop")
resourceClient.update(client, Collections.EMPTY_MAP, "stop")


