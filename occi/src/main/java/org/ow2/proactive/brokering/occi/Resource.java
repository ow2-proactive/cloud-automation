package org.ow2.proactive.brokering.occi;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Resource {
    public static final String OP_CREATE = "create";
    public static final String OP_UPDATE = "update";

    private static Logger logger = Logger.getLogger(Resource.class);

    private String uuid;
    private String category;
    private Map<String, String> attributes;
    private List<Action> links;

    public Resource() {}

    public Resource(String uuid, String category, Map<String, String> attributes) {
        this.uuid = uuid;
        this.category = category;
        this.attributes = attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getRelativePath() {
        return "/" + category + "/" + uuid;
    }

    public String getFullPath(String prefixUrl) {
        try {
            return new URL(prefixUrl + getRelativePath()).toString();
        } catch (MalformedURLException e) {
            logger.warn("Server url is not an URL", e);
            return getRelativePath();
        }
    }

    public String toString() {
        String result = "";
        TreeMap<String, String> map = new TreeMap<String, String>(attributes);
        for (String key : map.keySet()) {
            if (map.get(key) != null) {
                result += "X-OCCI-Attribute: " + key + "=\"" + map.get(key) + "\"\n";
            }
        }
        return result;
    }

    public boolean equals(Resource target) {
        if (!this.getUuid().equals(target.uuid))
            return false;

        if (!this.getCategory().equals(target.category))
            return false;

        if (this.getAttributes().size() != target.getAttributes().size())
            return false;

        for (String key: this.getAttributes().keySet()) {
            if (!target.getAttributes().containsKey(key))
                return false;
            String value = this.getAttributes().get(key);
            String valueTarget = target.getAttributes().get(key);
            if (!value.equals(valueTarget))
                return false;
        }

        return true;
    }

    public List<Action> getLinks() {
        return links;
    }

    public void setLinks(List<Action> links) {
        this.links = links;
    }

}
