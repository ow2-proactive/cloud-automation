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


package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import com.google.gson.Gson;

public class ReferencesBean {

    public List<ReferenceBean> references;

    public ReferencesBean() {
        references = Collections.EMPTY_LIST;
    }

    public ReferencesBean(References referencesSrc) {
        references = new ArrayList<ReferenceBean>();
        for (Reference r: referencesSrc) {
            references.add(new ReferenceBean(r));
        }
    }

    public References generateReferences() {
        References r = new References();
        for (ReferenceBean ref : references) {
            r.add(ref.generateReference());
        }
        return r;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

}
