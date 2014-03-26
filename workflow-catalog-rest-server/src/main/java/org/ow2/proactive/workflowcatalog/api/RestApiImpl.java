package org.ow2.proactive.workflowcatalog.api;

import javax.security.auth.login.LoginException;
import javax.ws.rs.FormParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper;
import org.ow2.proactive.workflowcatalog.security.HttpHeaderTokenSessionManager;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionContext;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.subject.Subject;


public class RestApiImpl implements RestApi {

    private static Logger logger = Logger.getLogger(RestApiImpl.class);

    /** For testing */
    static SchedulerProxyFactory schedulerProxyFactory = new SchedulerProxyFactory();

    @Override
    public String login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            SchedulerProxy scheduler = loginToSchedulerRestApi(username, password);
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

    private SchedulerProxy loginToSchedulerRestApi(String username,
      String password) throws LoginException, SchedulerRestException {
        SchedulerLoginData loginData = ConfigurationHelper.getSchedulerLoginData(
          ConfigurationHelper.getConfiguration());
        loginData.schedulerUsername = username;
        loginData.schedulerPassword = password;

        return schedulerProxyFactory.create(loginData);
    }

    private String internalLogin(String username, String password, SchedulerProxy scheduler,
      String sessionId) {
        Subject currentUser = createSubject(sessionId, scheduler);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        currentUser.login(token);

        return sessionId;
    }

    @Override
    public void logout() {
        try {
            SchedulerRestSession.getScheduler().disconnectFromScheduler();
        } catch (Exception e) {
            // will be cleaned by the REST API expiration mechanism if logout failed
            logger.warn("Failed to logout", e);
        }
        SecurityUtils.getSubject().logout();
    }

    private static Subject createSubject(String token, SchedulerProxy scheduler) {
        SessionContext sessionContext = new DefaultSessionContext();
        sessionContext.setSessionId(token);
        sessionContext.put(HttpHeaderTokenSessionManager.TOKEN_KEY, token);
        sessionContext.put(SchedulerRestSession.SCHEDULER_SESSION_KEY, scheduler);
        Session session = SecurityUtils.getSecurityManager().start(sessionContext);

        return new Subject.Builder().session(session).buildSubject();
    }
}
