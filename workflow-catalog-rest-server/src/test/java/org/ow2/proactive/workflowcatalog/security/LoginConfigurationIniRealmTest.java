package org.ow2.proactive.workflowcatalog.security;

import junit.framework.Assert;
import org.apache.shiro.config.Ini;
import org.junit.Test;
import java.io.IOException;

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
