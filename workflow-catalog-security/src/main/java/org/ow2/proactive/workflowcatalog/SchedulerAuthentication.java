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


package org.ow2.proactive.workflowcatalog;

import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.ow2.proactive.workflowcatalog.security.HttpHeaderTokenSessionManager;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionContext;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.subject.Subject;
import java.io.IOException;
import java.io.StringWriter;

public abstract class SchedulerAuthentication implements RestAuthentication {

    private static Logger logger = Logger.getLogger(SchedulerAuthentication.class);

    @Override
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/plain")
    public String login(@FormParam("username") String username, @FormParam("password") String password) {
        SchedulerLoginData data = new SchedulerLoginData(null, username, password, null, null);
        return commonLogin(data);
    }

    @Override
    public String loginWithCredential(@MultipartForm LoginForm multipart) {
        try {
            String username = multipart.getUsername();
            String credentials = getCredentialsAsString(multipart);

            SchedulerLoginData data = new SchedulerLoginData(null, username, null, credentials, null);

            return commonLogin(data);
        } catch (IOException e) {
            throw logAndThrowHttpException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String commonLogin(SchedulerLoginData login) {
        try {
            ISchedulerProxy scheduler = loginToSchedulerRestApi(login);
            String sessionId = scheduler.getSessionId();
            return internalLogin(login, scheduler, sessionId);
        } catch (LoginException e) {
            logger.warn("Could not login", e);
            throw logAndThrowHttpException(e, Response.Status.UNAUTHORIZED);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not login", e);
            throw logAndThrowHttpException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private WebApplicationException logAndThrowHttpException(Exception exception, Response.Status httpError) {
        logger.warn("Could not login", exception);
        return new WebApplicationException(httpError);
    }

    protected abstract ISchedulerProxy loginToSchedulerRestApi(SchedulerLoginData login) throws LoginException, SchedulerRestException;

    private String internalLogin(SchedulerLoginData login, ISchedulerProxy scheduler,
      String sessionId) throws LoginException {

        Subject currentUser = createSubject(sessionId, scheduler);

        // create token using username and,
        // either credentials or password
        String user = login.schedulerUsername;
        String pass = (login.schedulerCredentials!=null?
                login.schedulerCredentials:
                login.schedulerPassword);
        UsernamePasswordToken token =  new UsernamePasswordToken(user, pass);

        try {
            currentUser.login(token);
        } catch (IncorrectCredentialsException e) {
            throw new LoginException("Wrong password: " + e.getMessage());
        }
        return sessionId;
    }

    @Override
    @POST
    @Path("/logout")
    public void logout() {
        try {
            SchedulerRestSession.getScheduler().disconnectFromScheduler();
        } catch (Exception e) {
            // will be cleaned by the REST API expiration mechanism if logout failed
            logger.warn("Failed to logout", e);
        }
        SecurityUtils.getSubject().logout();
    }

    private static Subject createSubject(String token, ISchedulerProxy scheduler) {
        SessionContext sessionContext = new DefaultSessionContext();
        sessionContext.setSessionId(token);
        sessionContext.put(HttpHeaderTokenSessionManager.TOKEN_KEY, token);
        sessionContext.put(SchedulerRestSession.SCHEDULER_SESSION_KEY, scheduler);
        Session session = SecurityUtils.getSecurityManager().start(sessionContext);

        return new Subject.Builder().session(session).buildSubject();
    }

    private String getCredentialsAsString(LoginForm multipart) throws IOException {
        StringWriter credWriter = new StringWriter();
        IOUtils.copy(multipart.getCredential(), credWriter, "UTF8");
        return credWriter.toString();
    }

}
