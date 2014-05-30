package org.ow2.proactive.brokering.occi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.brokering.occi.categories.Categories;
import org.apache.log4j.Logger;

public class ResourceBuilder {

    private static Logger logger = Logger.getLogger(ResourceBuilder.class.getName());

    public static Resource factory(String uuid, String category, Map<String, String> attributes) {
        fillAttributes(category, attributes);
        return new Resource(uuid, category, attributes);
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
