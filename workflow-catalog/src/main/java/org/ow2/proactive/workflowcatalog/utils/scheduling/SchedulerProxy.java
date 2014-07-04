package org.ow2.proactive.workflowcatalog.utils.scheduling;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.security.KeyException;
import java.util.Map;

public class SchedulerProxy implements ISchedulerProxy {

    private static final Logger logger = Logger.getLogger(SchedulerProxy.class.getName());

    private final SchedulerRestClient restClient;
    private String sessionId;
    private SchedulerLoginData loginData;

    public SchedulerProxy(
      SchedulerLoginData schedulerLoginData) throws LoginException, SchedulerRestException {
        HttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();
        httpClient = new DefaultHttpClient(new PoolingClientConnectionManager(
          mgr.getSchemeRegistry()), params);

        loginData = schedulerLoginData;

        if (loginData.insecureMode)
            httpClient = HttpUtility.turnClientIntoInsecure(httpClient);

        this.restClient = new SchedulerRestClient(loginData.schedulerUrl,
          new ApacheHttpClient4Engine(httpClient));

        sessionId = connectToScheduler(loginData);
    }

    public TasksResults getAllTaskResults(String jobId)
      throws JobNotFinishedException, JobStatusRetrievalException {

        Map<String, String> results;
        try {
            try {
                results = restClient.getScheduler().jobResultValue(sessionId, jobId);
             } catch (NotConnectedRestException e) {
                sessionId = connectToScheduler(loginData);
                results = restClient.getScheduler().jobResultValue(sessionId, jobId);
            }
        } catch (Exception e) {
            throw new JobStatusRetrievalException(
                "Error getting result for job " + jobId +
                        " : " + e.getClass().getName() + " " + e.getMessage());
        }

        if (results == null)
            throw new JobNotFinishedException("No result for job " + jobId + " is available yet.");

        return new TasksResults(results);
    }

    public JobIdData submitJob(File jobFile) throws JobSubmissionException {
        try {
            try {
                return restClient.submitXml(sessionId, new FileInputStream(jobFile));
            } catch (NotConnectedRestException e) {
                sessionId = connectToScheduler(loginData);
                return restClient.submitXml(sessionId, new FileInputStream(jobFile));
            }
        } catch (Exception e) {
            throw new JobSubmissionException(e);
        }
    }

    private String connectToScheduler(
      SchedulerLoginData schedulerLoginData) throws LoginException, SchedulerRestException {
        logger.debug("Scheduler login process...");

        String user = schedulerLoginData.schedulerUsername;
        String pass = schedulerLoginData.schedulerPassword;
        String cred = schedulerLoginData.schedulerCredentials;

        String sessionId;
        if (cred != null && !cred.isEmpty()) {
            logger.debug("Using credentials mechanism for: " + user);
            LoginForm credForm = new LoginForm();
            credForm.setCredential(new ReaderInputStream(new StringReader(cred)));
            try {
                sessionId = restClient.getScheduler().loginWithCredential(credForm);
            } catch (KeyException e) {
                throw new LoginException("Invalid key: " + e.getMessage());
            }
        } else if (pass != null && !pass.isEmpty()) {
            logger.debug("Using username/password mechanism for: " + user);
            sessionId = restClient.getScheduler().login(user, pass);
        } else {
            throw new LoginException(
                    "Neither password nor credentials were provided for: " + user);
        }

        logger.debug("Scheduler session ID: " + sessionId);
        return sessionId;
    }

    public void disconnectFromScheduler() {
        if (sessionId == null)
            throw new IllegalArgumentException("sessionId cannot be null");

        try {
            restClient.getScheduler().disconnect(sessionId);
        } catch (Exception e) {
            // Best effort to disconnect.
        }
    }

    public String getSessionId() {
        return sessionId;
    }

}
