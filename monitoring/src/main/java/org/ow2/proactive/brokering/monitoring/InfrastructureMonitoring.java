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
