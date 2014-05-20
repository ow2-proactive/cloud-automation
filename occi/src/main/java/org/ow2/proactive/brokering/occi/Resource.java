package org.ow2.proactive.brokering.occi;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.workflowcatalog.References;
import org.apache.log4j.Logger;

public class Resource {
    public static final String OP_CREATE = "create";
    public static final String OP_READ = "read";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";

    private static Logger logger = Logger.getLogger(Resource.class.getName());
    private static Map<String, Resource> resources = new HashMap<String, Resource>();

    private String uuid;
    private String category;
    private Map<String, String> attributes;

    private Resource() {}

    private Resource(String uuid, String category, Map<String, String> attributes) {
        this.uuid = uuid;
        this.category = category;
        this.attributes = attributes;
    }

    public static Map<String, Resource> getResources() {
        return resources;
    }

    public static Resource factory(String uuid, String category, Map<String, String> attributes) {
        fillAttributes(category, attributes);
        Resource resource = new Resource(uuid, category, attributes);
        resources.put(uuid, resource);
        return resource;
    }

    public static void loadDatabase(List<Resource> resourceList) {
        for (Resource r : resourceList) {
            logger.debug("Loading a resource : r.getUuid() = " + r.getUuid());
            resources.put(r.getUuid(), r);
        }
    }

    private static void fillAttributes(String category, Map<String, String> attributes) {
        List<Attribute> genericAttributeList = getGenericAttributeList();
        genericAttributeList.addAll(getSpecificAttributeList(category));
        for (Attribute genericAttribute : genericAttributeList) {
            if (genericAttribute.isRequired() && !attributes.containsKey(genericAttribute.getName())) {
                logger.debug("Missing genericAttribute: " + genericAttribute.getName());
                continue;
            }
            if (!attributes.containsKey(genericAttribute.getName())) {
                attributes.put(genericAttribute.getName(), genericAttribute.getDefaultValue());
            }
        }
    }

    private static List<Attribute> getSpecificAttributeList(String category) {
        return Categories.fromString(category).getSpecificAttributeList();
    }

    private static List<Attribute> getGenericAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("occi.core.id", !mutable, required));
        attributeList.add(new Attribute("occi.core.location", mutable, !required));
        attributeList.add(new Attribute("action.state", mutable, !required));
        return attributeList;
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

    public URL getUrl(String prefix) {
        try {
            return new URL(prefix + category + "/" + uuid);
        } catch (Throwable e) {
            return null;
        }
    }

    public References create() throws Exception {
        return Broker.getInstance().request(category, OP_CREATE, getAttributes());
    }

    public References read() throws Exception {
        return Broker.getInstance().request(category, OP_READ, getAttributes());
    }

    public References update(String action) throws Exception {
        return Broker.getInstance().request(category, OP_UPDATE, action, getAttributes());
    }

    public References delete() throws Exception {
        return Broker.getInstance().request(category, OP_DELETE, getAttributes());
    }

    public List<Resource> getLinkedResources() {
        return null;
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

}
