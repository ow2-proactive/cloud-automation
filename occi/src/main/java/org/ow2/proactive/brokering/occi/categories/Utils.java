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


package org.ow2.proactive.brokering.occi.categories;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.ResourceLocation;
import org.apache.log4j.Logger;

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
            map.put(key, getString(json, key));
        }
        return map;
    }

    public static String getString(JsonObject json, String key) {

        try {
            return json.getString(key);
        } catch (ClassCastException e) { }

        try {
            return Integer.toString(json.getInt(key));
        } catch (ClassCastException e) { }

        try {
            return Boolean.toString(json.getBoolean(key));
        } catch (ClassCastException e) { }

        try {
            return json.getJsonArray(key).toString();
        } catch (ClassCastException e) { }

        try {
            return json.getJsonNumber(key).toString();
        } catch (ClassCastException e) { }

        try {
            return json.getJsonObject(key).toString();
        } catch (ClassCastException e) { }

        return "unknown_type";
    }

    public static String escapeAttribute(String attribute) {
        return attribute.replace(",", ";");
    }

    public static String toUrl(Response response) {
        return ((ResourceLocation) response.getEntity()).getLocation();
    }

    public static int countOccurrences(String str, String target) {
        return str.split("\\Q"+target+"\\E", -1).length - 1;
    }

    public static void checkResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Bad response (" + status + "):" + response.getEntity());
        }
    }

    public static File getScriptsPath(String path, String defaultPath) {
        File c = new File(path);
        if (!c.isDirectory()) {
            c = new File(Utils.class.getResource(defaultPath).getFile());
            logger.info("Using default scripts directory: " + c.getAbsolutePath());
        } else {
            logger.info("Using specified scripts directory: " + c.getAbsolutePath());
        }
        return c;
    }

}
