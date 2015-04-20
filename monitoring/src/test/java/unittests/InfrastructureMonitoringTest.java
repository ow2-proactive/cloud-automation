/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


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

