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
package org.ow2.proactive.workflowcatalog.api;

import javax.security.auth.login.LoginException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.workflowcatalog.WorkflowCatalogApplication;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IntegrationTest {

    private static Server server;

    private SchedulerProxy mockOfScheduler;
    private SchedulerProxyFactory schedulerProxyFactory;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void startServer() throws Exception {
        server = new Server(0);
        WebAppContext context = new WebAppContext("src/main/webapp", "/");

        // manually add resources because autoscan won't work in the embedded mode
        context.getInitParams().put("javax.ws.rs.Application", WorkflowCatalogApplication.class.getName());
        context.setParentLoaderPriority(true);

        server.addHandler(context);
        server.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }

    @Before
    public void injectSchedulerMock() throws LoginException, SchedulerRestException {
        mockOfScheduler = mock(SchedulerProxy.class);
        schedulerProxyFactory = mock(SchedulerProxyFactory.class);
        when(schedulerProxyFactory.create(
          Matchers.<org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData>any())).thenReturn(
          mockOfScheduler);
        WorkflowCatalogSchedulerAuthentication.schedulerProxyFactory = schedulerProxyFactory;
    }

    @Test
    public void access_to_anon_resource_is_authorized() throws Exception {
        Response response = httpGet("/");

        assertEquals(200, response.getStatus());
    }

    @Test
    public void access_to_protected_resource_is_denied() throws Exception {
        Response response = httpGet("/wc/workflow");

        assertEquals(401, response.getStatus());
    }

    @Test
    public void access_to_protected_resource_is_authorized_after_successful_login() throws Exception {
        when(mockOfScheduler.getSessionId()).thenReturn("abcd");

        Response login = login("demo", "demo");

        assertEquals(200, login.getStatus());
        assertEquals("abcd", login.readEntity(String.class));

        Response workflows = httpGet("/wc/workflow", "abcd");

        assertEquals(200, workflows.getStatus());
    }

    @Test
    public void login_failed_because_of_workflow_catalog_authentication() throws Exception {
        Response login = login("demo", "wrongpassword");

        assertEquals(401, login.getStatus());
    }

    @Test
    public void login_failed_because_of_scheduler_authentication() throws Exception {
        when(schedulerProxyFactory.create(Matchers.<SchedulerLoginData>any())).thenThrow(new LoginException("wrong pwd"));

        Response login = login("demo", "demo");

        assertEquals(401, login.getStatus());
    }

    @Test
    public void login_logout() throws Exception {
        when(mockOfScheduler.getSessionId()).thenReturn("abcd");

        Response login = login("demo", "demo");
        assertEquals(200, login.getStatus());
        assertEquals("abcd", login.readEntity(String.class));

        Response logout = httpPost("/wc/logout", "abcd");
        assertEquals(204, logout.getStatus());

        Response workflows = httpGet("/wc/workflow", "abcd");
        assertEquals(401, workflows.getStatus());
    }

    private Response login(String username, String password) {
        Form form = new Form();
        form.param("username", username);
        form.param("password", password);
        return request("/wc/login").
          post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    }

    private Response httpGet(String path) {
        return request(path).get();
    }

    private Response httpGet(String path, String sessionId) {
        return authenticatedRequest(path, sessionId).
          get();
    }

    private Response httpPost(String path, String sessionId) {
        return authenticatedRequest(path, sessionId).post(Entity.text("nada"));
    }

    private Invocation.Builder authenticatedRequest(String path, String sessionId) {
        return request(path).
          header("sessionid", sessionId);
    }

    private Invocation.Builder request(String path) {
        return ClientBuilder.newClient().
          target("http://localhost:" + server.getConnectors()[0].getLocalPort() + path).
          request();
    }

}
