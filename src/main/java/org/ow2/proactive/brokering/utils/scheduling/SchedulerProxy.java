package org.ow2.proactive.brokering.utils.scheduling;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Reference;

import org.apache.http.auth.AuthenticationException;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.apache.http.client.HttpClient;
import org.ow2.proactive.brokering.utils.HttpUtility;

public class SchedulerProxy {

    private static final Logger logger = Logger.getLogger(SchedulerProxy.class.getName());

    private static boolean insecureMode;
    private HttpClient httpClient;
    private SchedulerLoginData schedulerLoginData;


    public static void initializeSchedulerUtility(boolean insecureMode) {
        SchedulerProxy.insecureMode = insecureMode;
    }

    public SchedulerProxy(SchedulerLoginData schedulerLoginData) {
        httpClient = new DefaultHttpClient();
        if (insecureMode)
            HttpUtility.setInsecureAccess(httpClient);
        this.schedulerLoginData = schedulerLoginData;
    }

    public String getTaskResult(Reference r, String task) throws AuthenticationException, IOException {
        String jr = getAllTaskResults(r);
        JsonObject ob = Json.createReader(new StringReader(jr)).readObject();
        return ob.getString(task);
    }

    public JsonObject getAllTaskResultsAsJson(Reference r) throws AuthenticationException, IOException {
        String result = getAllTaskResults(r);
        JsonObject ob = Json.createReader(new StringReader(result)).readObject();
        return ob;
    }

    public String getAllTaskResults(Reference r) throws AuthenticationException, IOException {
        String sessid = connectToScheduler(schedulerLoginData);
        String endpoint = schedulerLoginData.getSchedulerUrl() + "/scheduler/jobs/" + r.getId() + "/result/value";
        HttpGet get = new HttpGet(endpoint);
        get.addHeader("sessionid", sessid);
        HttpResponse response = httpClient.execute(get);
        if (!HttpUtility.isSuccessStatusCode(response.getStatusLine().getStatusCode()))
            throw new IOException("Error getting result for JobId=" + r.getId() + " : " + response.getStatusLine());
        if (response.getEntity() == null)
            throw new IOException("No result for JobId=" + r.getId() + " available yet.");
        return EntityUtils.toString(response.getEntity());
    }

    public String submitJob(File jobFile) throws AuthenticationException, IOException {
        // Submit selected workflow to the Scheduler
        String sessid = connectToScheduler(schedulerLoginData);
        String endpoint = schedulerLoginData.getSchedulerUrl() + "/scheduler/submit";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("sessionid", sessid);
        MultipartEntity me = new MultipartEntity();
        me.addPart("descriptor", new FileBody(jobFile, "application/xml"));
        post.setEntity(me);
        HttpResponse response = httpClient.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        logger.info("Job submitted: " + result);

        return result;
    }


    private String connectToScheduler(SchedulerLoginData schedulerLoginData) throws AuthenticationException {
        // Authenticate to the Scheduler
        try {
            String endpoint = schedulerLoginData.getSchedulerUrl() + "/scheduler/login";
            HttpPost post = new HttpPost(endpoint);
            post.addHeader("Content-type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity("username=" + schedulerLoginData.getSchedulerUsername() + "&password=" + schedulerLoginData.getSchedulerPassword(), "UTF-8"));
            HttpResponse response = httpClient.execute(post);
            String sessid = EntityUtils.toString(response.getEntity());
            logger.debug("Scheduler Session ID: " + sessid);

            return sessid;

        } catch (Throwable e) {
            throw new AuthenticationException("Failed authenticating to the Scheduler", e);
        }
    }


}
