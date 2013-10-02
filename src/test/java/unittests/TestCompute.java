package unittests;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;

import javax.ws.rs.core.Response;
import java.util.UUID;

public class TestCompute {
    private static Logger logger = Logger.getLogger(TestCompute.class.getName());

    @Ignore
    @Test
    public void testCreateResource() throws Exception {
        String host = "127.0.0.1:8183";
        String category = "compute";
        String attributes = "sla=\"gold\",";
        attributes += "occi.compute.architecture=\"x64\",";
        attributes += "occi.compute.cores=\"2\",";
        attributes += "occi.compute.memory=\"4\",";
        attributes += "occi.compute.localstorage=\"60\",";
        attributes += "occi.compute.hostname=\"PITEST001\",";
        attributes += "occi.compute.template_name=\"windows-2008server-r2-en\"";

        OcciServer server = OcciServer.class.newInstance();
        Response result = server.createResource(host, category, attributes);
        System.out.println("CreateResource: result = " + result.getEntity());
    }

    @Ignore
    @Test
    public void testGetAllResources() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        System.out.println("GetAllResources: result = " + result.getEntity());
    }

    @Ignore
    @Test
    public void testGetResource() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        String[] urlTab = result.getEntity().toString().trim().split("/");
        String uuid = urlTab[urlTab.length - 1];
        //result = server.getResource("compute", uuid);
        System.out.println("GetResource: result =  " + result.getEntity());
    }

    @Ignore
    @Test
    public void testLoadResource() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        String[] urlTab = result.getEntity().toString().trim().split("/");
        String uuid = urlTab[urlTab.length - 1];
        Resource resource = Database.getInstance().load(UUID.fromString(uuid));
        System.out.println("LoadResource: resource = \n" + resource);
    }
}
