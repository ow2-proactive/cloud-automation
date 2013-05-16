package org.ow2.proactive.brokering.occi.infrastructure;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static Logger logger = Logger.getLogger(Utils.class.getName());

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
}
