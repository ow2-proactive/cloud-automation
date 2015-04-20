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

import junit.framework.Assert;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;
import org.ow2.proactive.workflowcatalog.api.SchedulerProxyFactory;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

import javax.security.auth.login.LoginException;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestSchedulerRealmTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String URL = "FAKE_URL";
    private static final Boolean INSECURE_MODE = false;
    private static final String SESSION_ID = "SESSION_ID";
    private static final String USER = "USER";
    private static final String PASS = "PASS";
    private static final String PASS_WRONG = "PASSx";

    private static final SchedulerLoginData LOGIN_CORRECT =
            new SchedulerLoginData(URL, USER, PASS, INSECURE_MODE);
    private static final SchedulerLoginData LOGIN_WRONG =
            new SchedulerLoginData(URL, USER, PASS_WRONG, INSECURE_MODE);

    @Test
    public void authorization_info_returned_is_correct() throws Exception {
        RestSchedulerRealm.MySecurityManagerService secMan = new RestSchedulerRealm.MySecurityManagerService(
                getSchedulerProxyFactory(), URL, INSECURE_MODE);

        RestSchedulerRealm r = new RestSchedulerRealm(secMan);

        PrincipalCollection principalCollection = new SimplePrincipalCollection(USER, PASS);
        AuthorizationInfo infoRight = r.doGetAuthorizationInfo(principalCollection);

        Assert.assertEquals(infoRight.getRoles(),
                            new HashSet<String>(Arrays.asList(RestSchedulerRealm.DEFAULT_ROLES)));

    }

    @Test
    public void authentication_info_returned_is_correct() throws Exception {
        RestSchedulerRealm.MySecurityManagerService secMan = new RestSchedulerRealm.MySecurityManagerService(
                getSchedulerProxyFactory(), URL, INSECURE_MODE);

        RestSchedulerRealm r = new RestSchedulerRealm(secMan);

        AuthenticationToken authenticationTokenRight = new UsernamePasswordToken(USER, PASS);
        AuthenticationInfo infoRight = r.doGetAuthenticationInfo(authenticationTokenRight);

        Assert.assertEquals(infoRight.getPrincipals().getPrimaryPrincipal(), USER);

        AuthenticationToken authenticationTokenWrong = new UsernamePasswordToken(USER, PASS_WRONG);
        exception.expect(AuthenticationException.class);
        AuthenticationInfo infoWrong = r.doGetAuthenticationInfo(authenticationTokenWrong);

    }

    private SchedulerProxyFactory getSchedulerProxyFactory()
            throws LoginException, SchedulerRestException {

        SchedulerProxyFactory schedFactory = mock(SchedulerProxyFactory.class);
        SchedulerProxy schedProxy = mock(SchedulerProxy.class);
        when(schedProxy.getSessionId()).thenReturn(SESSION_ID);

        when(schedFactory.create(argThat(new SimilarLoginDataThan(LOGIN_WRONG))))
                .thenThrow(new LoginException());
        doReturn(schedProxy).when(schedFactory)
                .create(argThat(new SimilarLoginDataThan(LOGIN_CORRECT)));

        return schedFactory;
    }


    class SimilarLoginDataThan extends ArgumentMatcher<SchedulerLoginData> {
        private SchedulerLoginData ref;
        public SimilarLoginDataThan(SchedulerLoginData ref) {
            this.ref = ref;
        }
        public boolean matches(Object target) {
            SchedulerLoginData t = (SchedulerLoginData)target;
            return matches(t.schedulerUsername, ref.schedulerUsername) &&
                    matches(t.schedulerPassword, ref.schedulerPassword) &&
                    matches(t.schedulerCredentials, t.schedulerCredentials);
        }
        public boolean matches(String a, String b) {
            return ((a ==  null && b == null) || (a !=  null && b != null && a.equals(b)));
        }
    }

}
