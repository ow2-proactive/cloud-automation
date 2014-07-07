package org.ow2.proactive.workflowcatalog;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive.workflowcatalog.security.HttpHeaderTokenSessionManager;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionContext;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.subject.Subject;

public abstract class SchedulerAuthentication implements RestAuthentication {

    private static Logger logger = Logger.getLogger(SchedulerAuthentication.class);

    @Override
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            ISchedulerProxy scheduler = loginToSchedulerRestApi(username, password);
            String sessionId = scheduler.getSessionId();

            try {
                return internalLogin(username, password, scheduler, sessionId);
            } catch (AuthenticationException e) {
                // Shiro failed login because of invalid credentials
                scheduler.disconnectFromScheduler();
                throw logAndThrowHttpException(e, Response.Status.UNAUTHORIZED);
            } catch (Exception e) {
                scheduler.disconnectFromScheduler();
                logger.warn("Could not login", e);
                throw logAndThrowHttpException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }

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

    protected abstract ISchedulerProxy loginToSchedulerRestApi(String username,
      String password) throws LoginException, SchedulerRestException;

    private String internalLogin(String username, String password, ISchedulerProxy scheduler,
      String sessionId) {
        Subject currentUser = createSubject(sessionId, scheduler);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        currentUser.login(token);

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
}
