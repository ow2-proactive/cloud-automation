package org.ow2.proactive.brokering.occi.categories.paas;

import org.ow2.proactive.brokering.occi.Attribute;

import java.util.ArrayList;
import java.util.List;

public class Instruction {

    public Instruction() {
    }

    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("occi.compute.instruction.path", mutable, !required));
        attributeList.add(new Attribute("occi.compute.instruction.arg1", mutable, !required));
        attributeList.add(new Attribute("occi.compute.instruction.arg2", mutable, !required));
        attributeList.add(new Attribute("occi.compute.instruction.arg3", mutable, !required));
        attributeList.add(new Attribute("occi.compute.instruction.arg4", mutable, !required));
        attributeList.add(new Attribute("occi.compute.instruction.arg5", mutable, !required));
        attributeList.add(new Attribute("occi.compute.proactive.node.token", mutable, !required));
        attributeList.add(new Attribute("occi.error.description", mutable, !required));
        return attributeList;
    }
}
