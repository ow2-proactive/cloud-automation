import org.junit.Ignore;
import org.ow2.proactive.brokering.Attributes;
import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.brokering.BrokeringServer;

import javax.ws.rs.core.Response;

public class TestBroker {

    Broker client;

    @org.junit.Before
    public void setUp() throws Exception {
//        client = ProxyFactory.create(Broker.class, "http://localhost:8080/broker/api");
        client = new BrokeringServer();
    }

    @org.junit.Test
    @Ignore
    public void testCatalog() throws Exception {
        client.getCatalog();
    }

    @org.junit.Test
    @Ignore
    public void testRule() throws Exception {
        client.getRules();
    }

    @org.junit.Test
    public void testRequestCreate() throws Exception {
        String action = "create";
        Attributes attributes = new Attributes();
        attributes.put("sla", "gold");

        attributes.put("occi.compute.architecture", "x64");
        attributes.put("occi.compute.cores", "2");
        attributes.put("occi.compute.memory", "4");
        attributes.put("occi.compute.localstorage", "60");
        attributes.put("occi.compute.hostname", "PITEST001");
        attributes.put("occi.compute.os", "windows-2008server-r2-en");
        attributes.put("occi.compute.state", "inactive");
        attributes.put("occi.core.id", "bf715a00-a1da-11e2-88c5-00505698695a");

        Response result = client.request(action, attributes);
        System.out.println("result = " + result);
    }
}
