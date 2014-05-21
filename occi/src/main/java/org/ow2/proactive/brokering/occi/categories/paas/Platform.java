package org.ow2.proactive.brokering.occi.categories.paas;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.BaseCategory;


public class Platform extends BaseCategory {

    public Platform() {

    }

    public List<Attribute> getAttributes() {
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
        attributeList.add(new Attribute("occi.compute.vendor.uuid", mutable, !required));
        attributeList.add(new Attribute("occi.compute.template_name", !mutable, required));
        attributeList.add(new Attribute("occi.compute.password", mutable, !required));
        attributeList.add(new Attribute("occi.compute.lease.stop", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.stop.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.networkinterface.address", mutable, !required));
        attributeList.add(new Attribute("occi.compute.error.code", mutable, !required, "0"));
        attributeList.add(new Attribute("occi.compute.error.description", mutable, !required));

        attributeList.add(new Attribute("occi.paas.state", mutable, !required));
        // Potential multi-host platform input
        attributeList.add(new Attribute("elasticity.vm.count.maximum", mutable, !required));
        attributeList.add(new Attribute("elasticity.vm.count.minimum", mutable, !required));
        attributeList.add(new Attribute("occi.paas.application.name", mutable, !required));
        // Potential multi-host platform output
        attributeList.add(new Attribute("occi.paas.application.endpoints", mutable, !required));

        attributeList.add(new Attribute("occi.paas.elasticity.masterplatform", mutable, !required));
        attributeList.add(new Attribute("occi.paas.elasticity.trigger", mutable, !required));

        attributeList.add(new Attribute("proactive.node.url", mutable, !required));

        attributeList.add(new Attribute("occi.error.description", mutable, !required));


        return attributeList;
    }

    @Override
    public String getScheme() {
        return "http://schemas.ogf.org/occi/platform#";
    }
}
