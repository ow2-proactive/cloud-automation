package org.ow2.proactive.brokering.occi.client.examples

import org.ow2.proactive.brokering.occi.client.OcciClient

OcciClient client = new OcciClient("http://localhost:8081/occi/api/occi");

def uuid = "1a1a2d09-90e5-48d7-8ae8-7e43c0f33f6a"
def args = new HashMap<String, String>()
args.put("application", "kibana")
def ret = client.updateResource("platform", uuid, args, "install");


