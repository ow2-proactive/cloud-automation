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


package org.ow2.proactive.brokering.occi.categories.trigger;

import java.util.List;
import java.util.ArrayList;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.BaseCategory;

public class ActionTrigger extends BaseCategory {

    public static final String OCCI_CORE_ID = "occi.core.id";
    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_INITACTION = "occi.monitoring.initaction";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_STOPACTION = "occi.monitoring.stopaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";
    public static final String OCCI_MONITORING_METADATA = "occi.monitoring.metadata";

    public ActionTrigger() { }

    @Override
    public List<Attribute> getAttributes() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute(OCCI_MONITORING_PERIODMS, mutable, !required));

        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_ACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_METADATA, mutable, !required));

        return attributeList;
    }

    @Override
    public String getScheme() {
        return "http://schemas.ogf.org/occi/#";
    }

}
