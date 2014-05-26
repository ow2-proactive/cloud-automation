package org.ow2.proactive.brokering.occi.categories;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.brokering.occi.Attribute;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * @param attributes map with the attributes
     * @return a String formatted like this : "a=3,b=4,c=5"
     */
    public static String buildString(Map<String, String> attributes) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(entry.getKey().replaceAll("\"",""));
            sb.append("=");
            sb.append(entry.getValue().replaceAll("\"",""));
            sb.append(",");
        }

        String output = sb.toString();

        if (output.endsWith(","))
            return output.substring(0, output.length()-1);
        else
            return output;
    }

    /**
     * @param attributes a String formatted like this : "a=3,b=4,c=5", if null, returned map is empty
     * @return
     */
    public static Map<String, String> buildMap(String attributes) {
        Map<String, String> map = new HashMap<String, String>();
        if (attributes != null && attributes.length() > 0) {
            for (String entry : attributes.split(",")) {
                String[] keyval = entry.split("=", 2);
                if (keyval != null && keyval.length == 2 && keyval[0] != null && keyval[1] != null) {
                    map.put(keyval[0].trim(), keyval[1].trim().replaceAll("\"", ""));
                }
            }
        }
        return map;
    }

    /**
     * @param attributeList
     * @param attributes    a String formatted like this : "a=3,b=4,c=5"
     * @return
     */
    public static Map<String, Attribute> buildAttributes(List<Attribute> attributeList, String attributes) {
        Map<String, String> map = buildMap(attributes);

        Map<String, Attribute> resultMap = new HashMap<String, Attribute>();
        for (Attribute attribute : attributeList) {
            String value = map.remove(attribute.getName());

            if (value == null) {  // attribute was not given
                if (attribute.isRequired()) {
                    throw new RuntimeException("Unable to build attributes. Required attribute is missing :" + attribute.getName());
                } else {
                    value = attribute.getDefaultValue();
                }
            }
            attribute.setValue(value);
            resultMap.put(attribute.getName(), attribute);
        }

        // Unexpected attributes are still in the map
        if (!map.isEmpty()) {
            String extra = "";
            for (String attr : map.keySet()) {
                extra += attr + " ";
            }
            logger.warn("Not expected attributes: " + extra);
        }

        return resultMap;
    }

    public static JsonObject convertToJson(String str) {
        return Json.createReader(new StringReader(str)).readObject();
    }

    public static Map<String, String> convertToMap(JsonObject json) {
        Map<String, String> map = new HashMap<String, String>();
        for (String key: json.keySet()) {
            String value = null;
            try {
                value = json.getString(key);
            } catch (ClassCastException e) {
                value = Integer.toString(json.getInt(key));
            }
            map.put(key, value);
        }
        return map;
    }


    public static Configuration getConfigurationTest() throws JAXBException {
        return getConfiguration("/config/configuration-test.xml");
    }

    public static Configuration getConfiguration() throws JAXBException {
        return getConfiguration("/config/configuration.xml");
    }

    public static Configuration getConfiguration(String file) throws JAXBException {
        File configFile = new File(Utils.class.getResource(file).getFile());
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Configuration) jaxbUnmarshaller.unmarshal(configFile);
    }

    public static String escapeAttribute(String attribute) {
        return attribute.replace(",", ";");
    }

    public static String toUrl(Response response) {
        String str = response.getEntity().toString();

        if (!str.contains("X-OCCI-Location: "))
            throw new RuntimeException("Bad url: " + str);

        return str.replace("X-OCCI-Location: ", "");
    }

    public static int countOccurrences(String str, String target) {
        return str.split("\\Q"+target+"\\E", -1).length - 1;
    }

    public static void checkResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 && status >= 300) {
            throw new RuntimeException("Bad response: " + response.getEntity().toString());
        }
    }

}
