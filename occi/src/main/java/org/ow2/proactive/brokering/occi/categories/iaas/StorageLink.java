package org.ow2.proactive.brokering.occi.categories.iaas;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.Category;

public class StorageLink implements Category {

    public StorageLink() {

    }

    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("occi.core.source", mutable, !required));
        attributeList.add(new Attribute("occi.core.target", mutable, !required));
        attributeList.add(new Attribute("occi.error.description", mutable, !required));
        return attributeList;
    }
}
