package org.ow2.proactive.brokering.occi.client.examples

import org.ow2.proactive.brokering.occi.client.OcciClient
import org.ow2.proactive.brokering.occi.client.ResourceInstance

OcciClient client = new OcciClient("http://localhost:8081/occi/api/occi");

/*
def uuid = "c06ffeb7-0053-4d40-8073-bd941068e17f"
def args = new HashMap<String, String>()
args.put("application", "kibana")
def ret = client.updateResource("platform", uuid, args, "install");
*/

Map<String, String> ar = new HashMap<String, String>();
def args = new HashMap<String, String>()
args.put("provider", "numergy")
args.put("rule", "numergy")
args.put("elasticity.vm.count.maximum", "2")
args.put("elasticity.vm.count.minimum", "1")
args.put("application", "elasticsearch")
args.put("flavor", "elastic")

ResourceInstance platformbig = null

platformbig = client.createResource("platform", args);
println "Big platform: " + platformbig.getLocation()
//def platformbig = new ResourceInstance("http://localhost:8081/occi/api/occi/platform/5997675a-3aa2-43ca-bd8e-320884686032");

//def uuid = "02892622-82af-4681-8d59-6e599eb71c3a"
def uuid = platformbig.getUuid()

platformbig = client.updateResource(platformbig.getCategory(), uuid, Collections.EMPTY_MAP, "stop");

println platformbig


