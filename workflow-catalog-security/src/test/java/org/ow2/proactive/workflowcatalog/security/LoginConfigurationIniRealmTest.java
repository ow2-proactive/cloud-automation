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


package org.ow2.proactive.workflowcatalog.security;

import java.io.IOException;

import junit.framework.Assert;
import org.apache.shiro.config.Ini;
import org.junit.Test;

public class LoginConfigurationIniRealmTest {

    private static final String LOGIN_CONFIG_FILE = "login.cfg";
    private static final String GROUP_CONFIG_FILE = "group.cfg";

    @Test
    public void users_list_is_correct() throws Exception {
        Ini.Section usersSection = getUsersSection();

        Assert.assertNotNull(usersSection.get("admin"));
        Assert.assertNotNull(usersSection.get("user"));

        Assert.assertEquals(usersSection.get("admin"), "adminpwd,usergrp,admingrp");
        Assert.assertEquals(usersSection.get("user"), "userpwd,usergrp");

    }

    private Ini.Section getUsersSection() throws IOException {
        String loginPath = resolveFilePath(LOGIN_CONFIG_FILE);
        String groupPath = resolveFilePath(GROUP_CONFIG_FILE);

        LoginConfigurationIniRealm iniRealm =
                new LoginConfigurationIniRealm();

        iniRealm.setLoginFilePath(loginPath);
        iniRealm.setGroupsFilePath(groupPath);

        return iniRealm.getUsersSection();
    }

    private String resolveFilePath(String cfgName) {
        String file = this.getClass().getResource(cfgName).getFile();
        if (file == null)
            throw new RuntimeException("Could not locate file '" + cfgName + "'");
        return file;
    }

}
