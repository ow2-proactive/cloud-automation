package org.ow2.proactive.brokering.monitoring;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MonitoringProxy {

    private String restUrl;
    private String restUsername;
    private String restPassword;
    private String nodeSourceName;
    private String jmxUrl;
    private HttpClient httpClient;

    public String getAttribute(String attribute) throws MonitoringException {
        String sessionId;
        try {
            sessionId = connectToRestRm(restUrl, restUsername, restPassword);
        } catch (AuthenticationException e) {
            throw new MonitoringException(e);
        }
        String output = getAttribute(attribute, sessionId);
        disconnectFromRestRm(sessionId);
        return output;
    }

    public String getAttribute(String attribute, String sessid) throws MonitoringException {
        HttpGet get = generateMonitoringHttpGet(attribute, sessid);
        try {
            HttpResponse response = httpClient.execute(get);
            return getResult(response);
        } catch (IOException e) {
            throw new MonitoringException("Cannot retrieve '" + attribute + "'", e);
        }
    }

    public String connectToRestRm(String restApi, String username, String password)
            throws AuthenticationException {

        try {
            HttpPost post = generateLoginHttpPost(restApi, username, password);
            HttpResponse response = httpClient.execute(post);
            return getResult(response);
        } catch (IOException e) {
            throw new AuthenticationException("Authentication failure: '" + username + "'", e);
        }
    }

    public Integer disconnectFromRestRm(String sessid) throws MonitoringException {
        try {
            HttpPost post = generateDisconnectLoginHttpPost(sessid);
            HttpResponse response = httpClient.execute(post);
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new MonitoringException("Disconnection failure", e);
        }
    }

    private HttpGet generateMonitoringHttpGet(String attributes, String sessionId) {
        String endpoint = generateMonitoringUri(attributes);
        HttpGet get = new HttpGet(endpoint);
        get = (HttpGet) addAuthenticationHeader(get, sessionId);
        return get;
    }

    // PRIVATE METHODS

    private HttpRequestBase addAuthenticationHeader(HttpRequestBase request, String sessionId) {
        request.addHeader("sessionid", sessionId);
        return request;
    }

    private String getResult(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpPost generateLoginHttpPost(String restApi, String username, String password)
            throws UnsupportedEncodingException {
        String endpoint = restApi + "/rm/login";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("Content-type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity("username=" + username + "&password=" + password, "UTF-8"));
        return post;
    }

    private HttpPost generateDisconnectLoginHttpPost(String sessid)
            throws UnsupportedEncodingException {
        String endpoint = restUrl + "/rm/disconnect";
        HttpPost post = new HttpPost(endpoint);
        post = (HttpPost) addAuthenticationHeader(post, sessid);
        return post;
    }

    private String generateMonitoringUri(String attributes) {
        ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("nodejmxurl", jmxUrl));
        nameValuePair.add(new BasicNameValuePair("objectname", generateObjectName(nodeSourceName)));
        nameValuePair.add(new BasicNameValuePair("attrs", attributes));
        return restUrl + "/rm/node/mbean?" + URLEncodedUtils.format(nameValuePair, "utf-8");
    }

    private String generateObjectName(String nodeSourceName) {
        return "ProActiveResourceManager:name=IaasMonitoring-" + nodeSourceName;
    }

    // BUILDER CONSTRUCTOR

    private MonitoringProxy(Builder builder) {

        restUrl = builder.restUrl;
        restUsername = builder.restUsername;
        restPassword = builder.restPassword;
        nodeSourceName = builder.nodeSourceName;
        jmxUrl = builder.jmxUrl;
        httpClient = new DefaultHttpClient();
        if (builder.insecureAccess)
            httpClient = HttpUtility.turnClientIntoInsecure(httpClient);

    }

    // BUILDER CLASS

    public static class Builder {

        private String restUrl;
        private String restUsername;
        private String restPassword;
        private String nodeSourceName;
        private String jmxUrl;
        private Boolean insecureAccess = false;

        public Builder() {
        }

        public Builder setRestUrl(String restUrl) {
            this.restUrl = restUrl;
            return this;
        }

        public Builder setCredentials(String restUsername, String restPassword) {
            this.restUsername = restUsername;
            this.restPassword = restPassword;
            return this;
        }

        public Builder setNodeSourceName(String nodeSourceName) {
            this.nodeSourceName = nodeSourceName;
            return this;
        }

        public Builder setJmxUrl(String jmxUrl) {
            this.jmxUrl = jmxUrl;
            return this;
        }

        public Builder setInsecureAccess() {
            this.insecureAccess = true;
            return this;
        }

        public MonitoringProxy build() {
            return new MonitoringProxy(this);
        }

    }
}
