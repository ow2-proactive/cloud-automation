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

    public Map<String, String> getHostProperties(String hostid) throws MonitoringException {
        return getMBeanAttributeAsMap("Host." + hostid);
    }

    public Map<String, String> getVMProperties(String vmid) throws MonitoringException {
        return getMBeanAttributeAsMap("VM." + vmid);
    }

    private Map<String, String> getMBeanAttributeAsMap(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonObject obj = extractObjectValueOfFirstElement(json);
        return transformToStringsMap(obj);
    }

    public String[] getHosts() throws MonitoringException {
        return getMBeanAttributeAsStringArray("Hosts");
    }

    public String[] getVMs() throws MonitoringException {
        return getMBeanAttributeAsStringArray("VMs");
    }

    private String[] getMBeanAttributeAsStringArray(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonArray array = extractArrayValueOfFirstElement(json);
        return transformToStringArray(array);
    }

    // PRIVATE METHODS

    private JsonObject extractObjectValueOfFirstElement(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonArray array = reader.readArray();
        JsonObject ob = array.getJsonObject(0);
        return ob.getJsonObject("value");
    }

    private JsonArray extractArrayValueOfFirstElement(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonArray array = reader.readArray();
        JsonObject ob = array.getJsonObject(0);
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

}
