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

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import javax.security.auth.login.LoginException;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.ow2.proactive.workflowcatalog.api.Configuration;
import org.ow2.proactive.workflowcatalog.api.SchedulerProxyFactory;
import org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

public class RestSchedulerRealm extends AuthorizingRealm {

    private static Logger logger = Logger.getLogger(RestSchedulerRealm.class.getName());
    public static String[] DEFAULT_ROLES = {"user"};

    private MySecurityManagerService mySecurityManagerService;

    public RestSchedulerRealm() {
        Configuration configuration = ConfigurationHelper.getConfiguration();

        mySecurityManagerService = new MySecurityManagerService(
                configuration.scheduler.url,
                configuration.security.insecuremode);
    }

    public RestSchedulerRealm(MySecurityManagerService securityManagerService) {
        this.mySecurityManagerService = securityManagerService;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo(new HashSet<String>(Arrays.asList(DEFAULT_ROLES)));
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authenticationToken) throws AuthenticationException {

        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;

        String user = authenticationToken.getPrincipal().toString();
        String pass = getPass(usernamePasswordToken.getPassword());
        String cred = getCred(usernamePasswordToken.getCredentials());

        if (user == null || user.isEmpty())
            throw new AuthenticationException("No user provided");

        MyPrincipal principal = null;
        try {
            if (pass != null) {
                principal =  mySecurityManagerService.findMyPrincipalByUsernamePassword(user, pass);
            } else if (cred != null) {
                principal = mySecurityManagerService.findMyPrincipalByUsernameCredentials(user, cred);
            } else {
                throw new LoginException("Neither pass nor credentials were provided for: " + user);
            }
        } catch (LoginException e) {
            throw new AuthenticationException("Login failed for user: " + user, e);
        } catch (SchedulerRestException e) {
            throw new AuthenticationException("REST error during login of user: " + user, e);
        }

        return new SimpleAccount(
                principal.getUsername(),
                principal.getCredentials(),
                getName(),
                principal.getRoles(),
                new HashSet());

    }

    private String getPass(char[] arr) {
        String pass = new String(arr);
        return (isPassword(pass)?pass:null);
    }

    private String getCred(Object obj) {
        String cred = new String((char[])obj);
        return (isCredential(cred)?cred:null);
    }

    private Boolean isPassword(String pass) {
        return (pass.length() < 50); // ugly way to determine if password or not
    }

    private Boolean isCredential(String cred) {
        return (cred.length() > 50); // ugly way to determine if credential or not
    }


    static class MySecurityManagerService {

        private String url;
        private SchedulerProxyFactory factory;
        private Boolean insecureMode;

        public MySecurityManagerService (String schedulerUrl, Boolean insecureMode) {
            this(new SchedulerProxyFactory(), schedulerUrl, insecureMode);
        }

        public MySecurityManagerService (SchedulerProxyFactory schedulerFactory, String schedulerUrl, Boolean insecureMode) {
            this.url = schedulerUrl;
            this.factory = schedulerFactory;
            this.insecureMode = insecureMode;
        }

        public MyPrincipal findMyPrincipalByUsernamePassword(
                String username, String password) throws LoginException, SchedulerRestException {
            SchedulerLoginData loginData = new SchedulerLoginData(url, username, password, insecureMode);
            SchedulerProxy schedulerProxy = factory.create(loginData);
            schedulerProxy.disconnectFromScheduler();
            return new MyPrincipal(username, password);
        }

        public MyPrincipal findMyPrincipalByUsernameCredentials(
                String username, String credentials) throws LoginException, SchedulerRestException {
            SchedulerLoginData loginData = new SchedulerLoginData(url, username, null, credentials, insecureMode);
            SchedulerProxy schedulerProxy = factory.create(loginData);
            schedulerProxy.disconnectFromScheduler();
            return new MyPrincipal(username, credentials);
        }

    }

    static class MyPrincipal {
        private String username;
        private String credentials;

        public MyPrincipal(String username, String credentials) {
            this.username = username;
            this.credentials = credentials;
        }

        public String getUsername() {
            return username;
        }

        public String getCredentials() {
            return credentials;
        }

        public Set<String> getRoles() {
            return new HashSet<String>(Arrays.asList(DEFAULT_ROLES));
        }

    }

}


