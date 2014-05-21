/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.brokering.occi.categories;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.iaas.Compute;
import org.ow2.proactive.brokering.occi.categories.iaas.Storage;
import org.ow2.proactive.brokering.occi.categories.iaas.StorageLink;
import org.ow2.proactive.brokering.occi.categories.paas.Platform;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;


public enum Categories {
    COMPUTE(new Compute()),
    STORAGE(new Storage()),
    STORAGE_LINK(new StorageLink()),
    PLATFORM(new Platform()),
    ACTION_TRIGGER(ActionTrigger.getInstance()),
    TEMPLATE(new Template());

    private Category category;

    Categories(Category category) {
        this.category = category;
    }

    public static Categories fromString(String categoryAsString) {
        for (Categories category : Categories.values()) {
            if (category.toString().equals(categoryAsString)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category name: " + categoryAsString);
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase().replaceAll("_", "");
    }

    public List<Attribute> getSpecificAttributeList() {
        return category.getAttributes();
    }

    public static List<Category> list() {
        List<Category> categories = new ArrayList<Category>();
        for (Categories category : values()) {
            categories.add(category.category);
        }
        return categories;
    }
}
