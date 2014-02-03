package org.ow2.proactive.brokering.monitoring;

import javax.json.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class InfrastructureMonitoring {

    private MonitoringProxy proxy;

    public InfrastructureMonitoring(MonitoringProxy proxy) {
        this.proxy = proxy;
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

    // PRIVATE METHODS

    private Map<String, String> getMBeanAttributeAsMap(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonObject obj = extractObjectValueOfFirstElement(json);
        return transformToStringsMap(obj);
    }

    private String[] getMBeanAttributeAsStringArray(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonArray array = extractArrayValueOfFirstElement(json);
        return transformToStringArray(array);
    }

    private JsonObject extractObjectValueOfFirstElement(String json) {
        JsonArray array = getJsonReader(json).readArray();
        JsonObject ob = getFirstJsonObject(array);
        return ob.getJsonObject("value");
    }

    private JsonArray extractArrayValueOfFirstElement(String json) {
        JsonArray array = getJsonReader(json).readArray();
        JsonObject ob = getFirstJsonObject(array);
        return ob.getJsonArray("value");
    }

    private String[] transformToStringArray(JsonArray array) {
        int i = 0;
        String[] output = new String[array.size()];
        for (JsonValue a : array)
            output[i++] = ((JsonString) a).getString();
        return output;
    }

    private Map<String, String> transformToStringsMap(JsonObject map) {
        Map<String, String> output = new HashMap<String, String>();
        for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
            output.put(entry.getKey(), ((JsonString) entry.getValue()).getString());
        }
        return output;
    }

    private JsonObject getFirstJsonObject(JsonArray array) {
        return array.getJsonObject(0);
    }

    private JsonReader getJsonReader(String json) {
        return Json.createReader(new StringReader(json));
    }

}
