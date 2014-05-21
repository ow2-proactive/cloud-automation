package org.ow2.proactive.brokering.occi.categories.iaas;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.BaseCategory;


public class StorageLink extends BaseCategory {

    public StorageLink() {

    }

    public List<Attribute> getAttributes() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("occi.core.source", mutable, !required));
        attributeList.add(new Attribute("occi.core.target", mutable, !required));
        attributeList.add(new Attribute("occi.error.description", mutable, !required));
        return attributeList;
    }

    @Override
    public String getScheme() {
        return "http://schemas.ogf.org/occi/infrastructure#";
    }
}
