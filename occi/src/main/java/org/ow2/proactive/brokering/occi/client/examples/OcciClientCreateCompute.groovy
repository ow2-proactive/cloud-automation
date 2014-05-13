import org.ow2.proactive.brokering.occi.client.OcciClient

OcciClient client = new OcciClient("http://try.activeeon.com/cloudautomation/");

// Create compute
def argu = ["provider":"openstack","rule":"try","occi.compute.hostname":"Vm-" + new Random().nextInt(10000)]
def compute = client.createResource("compute", argu);

// Wait for compute to be up
while (!"up".equalsIgnoreCase(compute.get("occi.compute.state"))) {
    Thread.sleep(1000*20)
    compute.refresh(client)
}

def ip = compute.get("occi.networkinterface.address")
println ip

// Stop compute
client.updateResource(compute, [:], "stop");



