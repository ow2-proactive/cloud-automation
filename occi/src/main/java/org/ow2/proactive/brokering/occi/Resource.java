package org.ow2.proactive.brokering.occi;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.brokering.occi.categories.iaas.*;
import org.ow2.proactive.brokering.occi.categories.paas.Platform;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import org.ow2.proactive.brokering.occi.categories.paas.Instruction;
import org.ow2.proactive.workflowcatalog.References;

import java.net.URL;
import java.util.*;

public class Resource {
    public static final String OP_CREATE = "create";
    public static final String OP_READ = "read";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";

    public static final String COMPUTE_CATEGORY_NAME = "compute";
    public static final String STORAGE_CATEGORY_NAME = "storage";
    public static final String STORAGE_LINK_CATEGORY_NAME = "storagelink";
    public static final String PLATFORM_CATEGORY_NAME = "platform";
    public static final String ACTION_TRIGGER_CATEGORY_NAME = "actiontrigger";
    public static final String INSTRUCTION_CATEGORY_NAME = "instruction";

    private static Logger logger = Logger.getLogger(Resource.class.getName());
    private static Map<UUID, Resource> resources = new HashMap<UUID, Resource>();

    private UUID uuid;
    private String category;
    private Map<String, String> attributes;

    private Resource(UUID uuid, String category, Map<String, String> attributes) {
        this.uuid = uuid;
        this.category = category;
        this.attributes = attributes;
    }

    public static Map<UUID, Resource> getResources() {
        return resources;
    }

    public static Resource factory(UUID uuid, String category, Map<String, String> attributes) {
        System.out.flush();
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
        if (category.equalsIgnoreCase(COMPUTE_CATEGORY_NAME)) {
            return new Compute().getSpecificAttributeList();
        } else if (category.equalsIgnoreCase(STORAGE_CATEGORY_NAME)) {
            return new Storage().getSpecificAttributeList();
        } else if (category.equalsIgnoreCase(STORAGE_LINK_CATEGORY_NAME)) {
            return new StorageLink().getSpecificAttributeList();
        } else if (category.equalsIgnoreCase(PLATFORM_CATEGORY_NAME)) {
            return new Platform().getSpecificAttributeList();
        } else if (category.equalsIgnoreCase(ACTION_TRIGGER_CATEGORY_NAME)) {
            return ActionTrigger.getInstance().getSpecificAttributeList();
        } else if (category.equalsIgnoreCase(INSTRUCTION_CATEGORY_NAME)) {
            return new Instruction().getSpecificAttributeList();
        }
        throw new IllegalArgumentException("Invalid category name: " + category);
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

    public UUID getUuid() {
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

}
