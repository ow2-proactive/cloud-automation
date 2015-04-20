/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


package org.ow2.proactive.brokering.occi.categories.iaas;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.BaseCategory;

import java.util.ArrayList;
import java.util.List;


public class Compute extends BaseCategory {

    public Compute() {

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
        attributeList.add(new Attribute("occi.compute.localstorage", !mutable, !required));
        attributeList.add(new Attribute("occi.compute.organization.name", mutable, !required));
        attributeList.add(new Attribute("occi.compute.vendor.location", mutable, !required));
        attributeList.add(new Attribute("occi.compute.vendor.uuid", mutable, !required));
        attributeList.add(new Attribute("occi.compute.vendor.vmpath", mutable, !required));
        attributeList.add(new Attribute("occi.compute.template_name", !mutable, required));
        attributeList.add(new Attribute("occi.compute.password", mutable, !required));
        attributeList.add(new Attribute("occi.compute.lease.stop", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.stop.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.compute.lease.delete.warning", mutable, !required, "-1"));
        attributeList.add(new Attribute("occi.networkinterface.address", mutable, !required));
        attributeList.add(new Attribute("occi.compute.proactive.node.token", mutable, !required));
        attributeList.add(new Attribute("occi.compute.error.code", mutable, !required, "0"));
        attributeList.add(new Attribute("occi.compute.error.description", mutable, !required));
        attributeList.add(new Attribute("occi.error.description", mutable, !required));
        attributeList.add(new Attribute("proactive.node.url", mutable, !required));
        return attributeList;
    }

    @Override
    public String getScheme() {
        return "http://schemas.ogf.org/occi/infrastructure#";
    }
}
