package org.ow2.proactive.workflowcatalog.utils.scheduling;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

public class SchedulerProxy {

    private static final Logger logger = Logger.getLogger(SchedulerProxy.class.getName());

    private HttpClient httpClient;
    private SchedulerLoginData schedulerLoginData;

    public SchedulerProxy(SchedulerLoginData schedulerLoginData) {

        this.schedulerLoginData = schedulerLoginData;
        this.httpClient = new DefaultHttpClient();
        if (this.schedulerLoginData.insecureMode)
            this.httpClient = HttpUtility.turnClientIntoInsecure(this.httpClient);
    }

    public String getTaskResult(Reference r, String task)
            throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {

        String jr = getAllTaskResults(r);
        JsonObject ob = Json.createReader(new StringReader(jr)).readObject();
        if (ob == null)
            throw new JobStatusRetrievalException("Could not read json task result object: " + jr);
        return ob.getString(task);
    }

    public JsonObject getAllTaskResultsAsJson(Reference r)
            throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {

        String result = getAllTaskResults(r);
        JsonObject ob = Json.createReader(new StringReader(result)).readObject();
        if (ob == null)
            throw new JobStatusRetrievalException(
                    "Could not read json task result object: " + result);
        return ob;
    }

    public String getAllTaskResults(Reference r)
            throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {

        String sessionId = connectToScheduler(schedulerLoginData);
        String endpoint =
                schedulerLoginData.schedulerUrl + "/scheduler/jobs/" + r.getId() + "/result/value";
        HttpGet get = new HttpGet(endpoint);
        get.addHeader("sessionid", sessionId);

        HttpResponse response = null;
        try {
            response = httpClient.execute(get);
            if (HttpUtility.isNotSuccessStatusCode(response.getStatusLine().getStatusCode()))
                throw new JobStatusRetrievalException(
                        "Error getting result for " +
                                "JobId=" + r.getId() + " : " + response.getStatusLine());
            if (response.getEntity() == null)
                throw new JobNotFinishedException(
                        "No result for JobId=" + r.getId() + " available yet.");
            return EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            throw new JobStatusRetrievalException("Failed to retrieve status: " + r, e);
        } finally {
            consumeResponse(response);
            disconnectFromScheduler(sessionId);
        }
    }

    public JobSubmissionResponse submitJob(File jobFile) throws AuthenticationException, JobSubmissionException {
        String endpoint = schedulerLoginData.schedulerUrl + "/scheduler/submit";
        String sessionId = connectToScheduler(schedulerLoginData);
        HttpPost post = buildPostForJobSubmission(sessionId, jobFile, endpoint);
        return executePostForJobSubmission(post);
    }

    private JobSubmissionResponse executePostForJobSubmission(HttpPost post) throws JobSubmissionException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
            String resultJson = EntityUtils.toString(response.getEntity());
            return new JobSubmissionResponse(resultJson);
        } catch (IOException e) {
            throw new JobSubmissionException(e);
        } finally {
            consumeResponse(response);
        }
    }

    private HttpPost buildPostForJobSubmission(String sessionId, File jobFile, String endpoint) {
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("sessionid", sessionId);
        MultipartEntity me = new MultipartEntity();
        me.addPart("descriptor", new FileBody(jobFile, "application/xml"));
        post.setEntity(me);
        return post;
    }

    private String connectToScheduler(SchedulerLoginData schedulerLoginData)
            throws AuthenticationException {

        HttpResponse response = null;

        try {
            String endpoint = schedulerLoginData.schedulerUrl + "/scheduler/login";
            HttpPost post = new HttpPost(endpoint);
            post.addHeader("Content-type", "application/x-www-form-urlencoded");
            post.setEntity(
                    new StringEntity(
                            "username=" + schedulerLoginData.schedulerUsername +
                                    "&password=" + schedulerLoginData.schedulerPassword,
                            "UTF-8"));
            response = httpClient.execute(post);

            String sessionId = EntityUtils.toString(response.getEntity());
            logger.debug("Scheduler session ID: " + sessionId);
            return sessionId;

        } catch (IOException e) {
            throw new AuthenticationException("Failed authenticating to the Scheduler", e);
        } finally {
            consumeResponse(response);
        }
    }

    private void consumeResponse(HttpResponse response) {
        if (response != null)
            try {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                // Ignore it.
            }
    }

    public void disconnectFromScheduler(String sessionId) {
        if (sessionId == null)
            throw new IllegalArgumentException("sessionId cannot be null");

        String endpoint = schedulerLoginData.schedulerUrl + "/rm/login";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("Content-type", "application/x-www-form-urlencoded");

        try {
            post.setEntity(new StringEntity(
                    "username=" + schedulerLoginData.schedulerUsername +
                            "&password=" + schedulerLoginData.schedulerPassword, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(
                    schedulerLoginData.schedulerUsername + ":"
                            + schedulerLoginData.schedulerPassword);
        }

        try {
            HttpResponse response = httpClient.execute(post);
            consumeResponse(response);
        } catch (IOException e) {
            // Best effort to disconnect.
        }
    }


}
