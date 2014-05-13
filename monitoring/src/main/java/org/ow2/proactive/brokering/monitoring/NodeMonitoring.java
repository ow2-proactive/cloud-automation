package org.ow2.proactive.brokering.monitoring;

import javax.json.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class NodeMonitoring {

    private MonitoringProxy proxy;

    public NodeMonitoring(MonitoringProxy proxy) {
        this.proxy = proxy;
    }

    public Map<String, String> getMBeanAttributeAsMap(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonObject obj = extractObjectValueOfFirstElement(json);
        return transformToStringsMap(obj);
    }

    public String[] getMBeanAttributeAsStringArray(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonArray array = extractArrayValueOfFirstElement(json);
        return transformToStringArray(array);
    }

    public String getMBeanAttributeAsString(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonObject ob = getSingleObject(json);
        return ob.getString("value");
    }

    public Integer getMBeanAttributeAsInteger(String attribute) throws MonitoringException {
        String json = proxy.getAttribute(attribute);
        JsonObject ob = getSingleObject(json);
        return ob.getInt("value");
    }

    // PRIVATE METHODS

    private JsonObject extractObjectValueOfFirstElement(String json) throws MonitoringException {
        JsonObject ob = getSingleObject(json);
        return ob.getJsonObject("value");
    }

    private JsonArray extractArrayValueOfFirstElement(String json) throws MonitoringException{
        JsonObject ob = getSingleObject(json);
        return ob.getJsonArray("value");
    }

    private JsonObject getSingleObject(String json) throws MonitoringException {
        JsonArray array = getJsonReader(json).readArray();
        return getFirstJsonObject(array);
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

    private JsonObject getFirstJsonObject(JsonArray array) throws MonitoringException {
        try {
            return array.getJsonObject(0);
        } catch (IndexOutOfBoundsException e) {
            throw new MonitoringException("Invalid reply");
        }
    }

    private JsonReader getJsonReader(String json) {
        return Json.createReader(new StringReader(json));
    }

}
