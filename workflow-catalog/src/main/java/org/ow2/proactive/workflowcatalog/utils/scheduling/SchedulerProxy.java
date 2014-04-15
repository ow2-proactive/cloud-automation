package org.ow2.proactive.workflowcatalog.utils.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;


public class SchedulerProxy {

    private static final Logger logger = Logger.getLogger(SchedulerProxy.class.getName());

    private final SchedulerRestClient restClient;
    private final String sessionId;

    public SchedulerProxy(
      SchedulerLoginData schedulerLoginData) throws LoginException, SchedulerRestException {
        HttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();
        httpClient = new DefaultHttpClient(new PoolingClientConnectionManager(
          mgr.getSchemeRegistry()), params);

        if (schedulerLoginData.insecureMode)
            httpClient = HttpUtility.turnClientIntoInsecure(httpClient);

        this.restClient = new SchedulerRestClient(schedulerLoginData.schedulerUrl,
          new ApacheHttpClient4Engine(httpClient));

        sessionId = connectToScheduler(schedulerLoginData);
    }

    public Map<String, String> getAllTaskResults(String jobId)
      throws JobNotFinishedException, JobStatusRetrievalException {

        Map<String, String> jobResultValue = null;
        try {
            jobResultValue = restClient.getScheduler().jobResultValue(sessionId, jobId);
        } catch (Exception e) {
            throw new JobStatusRetrievalException(
                "Error getting result for job " + jobId + " : " + e.getMessage());
        }

        if (jobResultValue == null)
            throw new JobNotFinishedException("No result for job " + jobId + " is available yet.");

        return jobResultValue;

    }

    public JobIdData submitJob(File jobFile) throws JobSubmissionException {
        try {
            return restClient.submitXml(sessionId, new FileInputStream(jobFile));
        } catch (Exception e) {
            throw new JobSubmissionException(e);
        }
    }

    private String connectToScheduler(
      SchedulerLoginData schedulerLoginData) throws LoginException, SchedulerRestException {
        String sessionId = restClient.getScheduler().login(schedulerLoginData.schedulerUsername,
          schedulerLoginData.schedulerPassword);
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
