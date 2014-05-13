package unittests;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.brokering.monitoring.InfrastructureMonitoring;
import org.ow2.proactive.brokering.monitoring.MonitoringException;
import org.ow2.proactive.brokering.monitoring.MonitoringProxy;
import org.ow2.proactive.brokering.monitoring.NodeMonitoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeMonitoringTest {

    private static MonitoringProxy proxyMock;

    @BeforeClass
    public static void beforeClass() throws Exception {
        initializeProxyMock();
    }

    private static void initializeProxyMock() throws IOException, MonitoringException {
        Properties restResponses = new Properties();
        restResponses.load(NodeMonitoringTest.class.getResourceAsStream(
                "/properties/nodemonitoring.properties"));
        proxyMock = mock(MonitoringProxy.class);
        for (Object attribute : restResponses.keySet())
            when(proxyMock.getAttribute(attribute.toString())).thenReturn(
                    restResponses.get(attribute.toString()).toString());
    }

    @Test
    public void getInteger_Test() throws Exception {
        NodeMonitoring m = new NodeMonitoring(proxyMock);
        Integer irq = m.getMBeanAttributeAsInteger("Irq");
        Assert.assertEquals(irq, new Integer(0));
    }

    @Test
    public void geString_Test() throws Exception {
        NodeMonitoring m = new NodeMonitoring(proxyMock);
        String model = m.getMBeanAttributeAsString("Model");
        Assert.assertEquals(model, "Opteron");
    }

    @Test
    public void getMap_Test() throws Exception {
        NodeMonitoring m = new NodeMonitoring(proxyMock);
        Map<String, String> map = m.getMBeanAttributeAsMap("PFlags");
        Assert.assertEquals(map.keySet().size(), 1);
        Assert.assertEquals(map.get("keyfile"), "filecontent");
    }



}

