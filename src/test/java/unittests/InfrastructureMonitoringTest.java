package unittests;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.brokering.monitoring.InfrastructureMonitoring;
import org.ow2.proactive.brokering.monitoring.MonitoringException;
import org.ow2.proactive.brokering.monitoring.MonitoringProxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InfrastructureMonitoringTest {

    private static MonitoringProxy proxyMock;

    @BeforeClass
    public static void beforeClass() throws Exception {
        initializeProxyMock();
    }

    private static void initializeProxyMock() throws IOException, MonitoringException {
        Properties restResponses = new Properties();
        restResponses.load(InfrastructureMonitoringTest.class.getResourceAsStream(
                "/properties/infrastructuremonitoring.properties"));
        proxyMock = mock(MonitoringProxy.class);
        for (Object attribute : restResponses.keySet())
            when(proxyMock.getAttribute(attribute.toString())).thenReturn(
                    restResponses.get(attribute.toString()).toString());
    }

    @Test
    public void getHosts_Test() throws Exception {
        InfrastructureMonitoring m = new InfrastructureMonitoring(proxyMock);
        String[] hosts = m.getHosts();
        Assert.assertEquals(hosts.length, 2);
        Assert.assertTrue(Arrays.asList(hosts).contains("host1"));
        Assert.assertTrue(Arrays.asList(hosts).contains("host2"));
    }

    @Test
    public void getVMs_Test() throws Exception {
        InfrastructureMonitoring m = new InfrastructureMonitoring(proxyMock);
        String[] vms = m.getVMs();
        Assert.assertEquals(vms.length, 2);
        Assert.assertTrue(Arrays.asList(vms).contains("vm1"));
        Assert.assertTrue(Arrays.asList(vms).contains("vm2"));
    }

    @Test
    public void getHostProperties_Test() throws Exception {
        InfrastructureMonitoring m = new InfrastructureMonitoring(proxyMock);
        Map<String, String> hp = m.getHostProperties("host1");
        Assert.assertTrue(hp.get("pflags.nodename").equals("host1"));
    }

    @Test
    public void getVMProperties_Test() throws Exception {
        InfrastructureMonitoring m = new InfrastructureMonitoring(proxyMock);
        Map<String, String> vp = m.getVMProperties("vm1");
        Assert.assertTrue(vp.get("pflags.nodename").equals("vm1"));
    }
}

