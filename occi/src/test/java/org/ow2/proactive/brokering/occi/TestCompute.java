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

import javax.ws.rs.core.Response;

import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // FIXME TODO test with no asserts?
public class TestCompute {
    private static Logger logger = Logger.getLogger(TestCompute.class.getName());

    @Test
    public void testCreateResource() throws Exception {
        String host = "127.0.0.1:8183";
        String category = "compute";
        String attributes = "sla=\"gold\",";
        attributes += "occi.compute.architecture=\"x64\",";
        attributes += "occi.compute.cores=\"2\",";
        attributes += "occi.compute.memory=\"4\",";
        attributes += "occi.compute.localstorage=\"60\",";
        attributes += "occi.compute.hostname=\"PITEST001\",";
        attributes += "occi.compute.template_name=\"windows-2008server-r2-en\"";

        OcciServer server = null; // TODO create with mocks
        Response result = server.createResource(host, category, attributes);
        System.out.println("CreateResource: result = " + result.getEntity());
    }

    @Test
    public void testGetAllResources() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        System.out.println("GetAllResources: result = " + result.getEntity());
    }

    @Test
    public void testGetResource() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        String[] urlTab = result.getEntity().toString().trim().split("/");
        String uuid = urlTab[urlTab.length - 1];
        //result = server.getResource("compute", uuid);
        System.out.println("GetResource: result =  " + result.getEntity());
    }

    @Test
    public void testLoadResource() throws Exception {
        OcciServer server = OcciServer.class.newInstance();
        Response result = server.getAllResources("compute");
        String[] urlTab = result.getEntity().toString().trim().split("/");
        String uuid = urlTab[urlTab.length - 1];
        Database db = new DatabaseFactory().build();
        Resource resource = db.load(uuid);
        System.out.println("LoadResource: resource = \n" + resource);
        db.close();
    }
}
