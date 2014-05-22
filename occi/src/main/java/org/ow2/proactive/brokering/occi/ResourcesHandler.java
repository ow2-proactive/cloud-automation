package org.ow2.proactive.brokering.occi;

import java.util.*;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.Categories;

public class ResourcesHandler {

    private static Logger logger = Logger.getLogger(ResourcesHandler.class.getName());
    private static Map<String, Resource> resources = new HashMap<String, Resource>();

    public static Map<String, Resource> getResources() {
        if (resources == null) {
            resources = new HashMap<String, Resource>();
            loadDatabase(Database.getDatabase());
        }
        return resources;
    }

    public static Resource factory(String uuid, String category, Map<String, String> attributes) {
        fillAttributes(category, attributes);
        Resource resource = new Resource(uuid, category, attributes);
        resources.put(uuid, resource);
        return resource;
    }

    private static void loadDatabase(Database db) {
        List<Resource> resourceList = db.getAllResources();
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

}
