package org.ow2.proactive.brokering.occi.client.examples

import org.ow2.proactive.brokering.occi.client.OcciClient

OcciClient client = new OcciClient("http://try.activeeon.com/cloudautomation/");

def uuid = "1a1a2d09-90e5-48d7-8ae8-7e43c0f33f6a"
//def args = new HashMap<String, String>()
def args = [application:"kibana"]
//args.put("application", "kibana")
def ret = client.updateResource("platform", uuid, args, "install");


