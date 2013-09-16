package org.ow2.proactive.brokering.occi.infrastructure;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Attribute;

import javax.json.Json;
import javax.json.JsonObject;
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
            sb.append(entry.getKey().replaceAll("\"","") + "=" + entry.getValue().replaceAll("\"","") + ",");
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

    /**
     * @param str a json formatted string
     * @return the json object
     */
    public static JsonObject convertToJson(String str) {
        return Json.createReader(new StringReader(str)).readObject();
    }

    /**
     * @param json json to be converted to a map
     * @return resulting map
     */
    public static Map<String, String> convertToMap(JsonObject json) {
        Map<String, String> o = new HashMap<String, String>();
        for (String key : json.keySet())
            o.put(key, json.getString(key));
        return o;
    }

}
