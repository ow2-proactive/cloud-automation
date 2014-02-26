package org.ow2.proactive.brokering.occi.categories.paas;

import org.ow2.proactive.brokering.occi.Attribute;

import java.util.ArrayList;
import java.util.List;

public class Platform {

    public Platform() {

    }

    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute("sla", mutable, !required, "silver"));
        attributeList.add(new Attribute("occi.compute.architecture", !mutable, !required));
        attributeList.add(new Attribute("occi.compute.cores", mutable, !required));
        attributeList.add(new Attribute("occi.compute.hostname", !mutable, !required));
        attributeList.add(new Attribute("occi.compute.memory", mutable, !required));
        attributeList.add(new Attribute("occi.compute.state", mutable, !required, "inactive"));
        attributeList.add(new Attribute("occi.compute.localstorage", !mutable, !required, "20"));
        attributeList.add(new Attribute("occi.compute.organization.name", mutable, !required));
        attributeList.add(new Attribute("occi.compute.vendor.location", mutable, !required));
        attributeList.add(new Attribute("occi.compute.vendor.vmpath", mutable, !required));
        attributeList.add(new Attribute("occi.compute.template_name", !mutable, required));
        attributeList.add(new Attribute("occi.compute.password", mutable, !required));
        attributeList.add(new Attribute("occi.compute.lease.stop", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.stop.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.networkinterface.address", mutable, !required));
        attributeList.add(new Attribute("occi.compute.error.code", mutable, !required, "0"));
        attributeList.add(new Attribute("occi.compute.error.description", mutable, !required));

        attributeList.add(new Attribute("occi.paas.status", mutable, !required));
        // Potential multi-host platform input
        attributeList.add(new Attribute("occi.paas.vm.count.maximum", mutable, !required));
        attributeList.add(new Attribute("occi.paas.vm.count.minimum", mutable, !required));
        attributeList.add(new Attribute("occi.paas.application.name", mutable, !required));
        // Potential multi-host platform output
        attributeList.add(new Attribute("occi.paas.application.endpoints", mutable, !required));

        attributeList.add(new Attribute("occi.paas.elasticity.base", mutable, !required));
        attributeList.add(new Attribute("occi.paas.elasticity.trigger", mutable, !required));


        return attributeList;
    }
}
