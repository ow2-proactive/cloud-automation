package org.ow2.proactive.brokering.occi.categories.iaas;

import org.ow2.proactive.brokering.occi.Attribute;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    public Storage() {

    }

    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("occi.storage.size", mutable, required));
        attributeList.add(new Attribute("occi.core.target", mutable, !required));
        return attributeList;
    }
}
