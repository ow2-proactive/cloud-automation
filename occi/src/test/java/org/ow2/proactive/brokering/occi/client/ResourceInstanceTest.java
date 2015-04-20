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


package org.ow2.proactive.brokering.occi.client;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;

public class ResourceInstanceTest {

    private static final String IN_HEADER_EXAMPLE = "X-OCCI-Location: http://10.200.96.143:8081/occi/api/occi/platform/7cb93650-42e6-4450-913c-299caf871299";
    private static final String LOCATION_EXAMPLE = "http://10.200.96.143:8081/occi/api/occi/platform/7cb93650-42e6-4450-913c-299caf871299";
    private static final String UUID_EXAMPLE = "7cb93650-42e6-4450-913c-299caf871299";
    private static final String CATEGORY_EXAMPLE = "platform";

    @Test
    public void constructorWithXOcciAttributePrefix_Test() throws Exception {
        ResourceInstance resource = new ResourceInstance(IN_HEADER_EXAMPLE);
        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
        Assert.assertTrue(resource.getLocation().equals(LOCATION_EXAMPLE));
        Assert.assertTrue(resource.getCategory().equals(CATEGORY_EXAMPLE));
    }

    @Test
    public void constructorWithLocation_Test() throws Exception {
        ResourceInstance resource = new ResourceInstance(LOCATION_EXAMPLE);
        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
        Assert.assertTrue(resource.getLocation().equals(LOCATION_EXAMPLE));
        Assert.assertTrue(resource.getCategory().equals(CATEGORY_EXAMPLE));
    }

    @Test
    public void constructorWithAttributes_Test() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("occi.core.id", UUID_EXAMPLE);
        ResourceInstance resource = new ResourceInstance(map);

        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
    }

}
