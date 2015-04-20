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

