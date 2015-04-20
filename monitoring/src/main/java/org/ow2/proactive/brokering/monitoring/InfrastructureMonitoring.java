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


package org.ow2.proactive.brokering.monitoring;

import java.util.Map;

public class InfrastructureMonitoring extends NodeMonitoring {

    public InfrastructureMonitoring(MonitoringProxy proxy) {
        super(proxy);
    }

    public Map<String, String> getHostProperties(String hostId) throws MonitoringException {
        return getMBeanAttributeAsMap("Host." + hostId);
    }

    public Map<String, String> getVMProperties(String vmId) throws MonitoringException {
        return getMBeanAttributeAsMap("VM." + vmId);
    }

    public String[] getHosts() throws MonitoringException {
        return getMBeanAttributeAsStringArray("Hosts");
    }

    public String[] getVMs() throws MonitoringException {
        return getMBeanAttributeAsStringArray("VMs");
    }

    public String getVMSuchThat(String property, String value) throws MonitoringException {
        for (String vmid : getVMs())
            if (getVMProperties(vmid).get(property).equals(value))
                return vmid;
        return null;
    }

}
