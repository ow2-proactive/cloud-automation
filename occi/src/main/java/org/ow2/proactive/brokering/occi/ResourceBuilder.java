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
