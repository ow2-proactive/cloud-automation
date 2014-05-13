package org.ow2.proactive.brokering.monitoring;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MonitoringProxy {

    private String restUrl;
    private String restCredentials;
    private String restUsername;
    private String restPassword;
    private String nodeSourceName;
    private String objectName;
    private String type;
    private String jmxUrl;
    private HttpClient httpClient;

    public String getAttribute(String attribute) throws MonitoringException {
        String sessionId;
        try {
            sessionId = connectToRestRm(restUrl, restCredentials, restUsername, restPassword);
        } catch (AuthenticationException e) {
            throw new MonitoringException(e);
        }
        String output = getAttributeWithSession(attribute, sessionId);
        disconnectFromRestRm(sessionId);
        return output;
    }

    // PRIVATE METHODS

    private String getAttributeWithSession(String attribute, String sessid) throws MonitoringException {
        HttpGet get = generateMonitoringHttpGet(attribute, sessid);
        try {
            HttpResponse response = httpClient.execute(get);
            return getResult(response);
        } catch (IOException e) {
            throw new MonitoringException("Cannot retrieve '" + attribute + "'", e);
        }
    }

    private String connectToRestRm(String restApi, String credentials, String username, String password)
            throws AuthenticationException {

        try {
            HttpPost post = generateLoginHttpPost(restApi, credentials, username, password);
            HttpResponse response = httpClient.execute(post);
            return getResult(response);
        } catch (IOException e) {
            throw new AuthenticationException("Authentication failure: '" + username + "'", e);
        }
    }

    private Integer disconnectFromRestRm(String sessid) throws MonitoringException {
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

    private HttpRequestBase addAuthenticationHeader(HttpRequestBase request, String sessionId) {
        request.addHeader("sessionid", sessionId);
        return request;
    }

    private String getResult(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpPost generateLoginHttpPost(String restApi, String credentials, String username, String password)
            throws IOException {
        String endpoint = restApi + "/rm/login";
        HttpPost post = new HttpPost(endpoint);
        if (credentials == null) {
            MultipartEntity m = new MultipartEntity();
            m.addPart("username", new StringBody(username));
            m.addPart("password", new StringBody(password));
            post.setEntity(m);
        } else {
            MultipartEntity m = new MultipartEntity();
            m.addPart("credential", new StringBody(credentials));
            post.setEntity(m);
        }
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
        nameValuePair.add(new BasicNameValuePair("objectname", generateObjectName()));
        nameValuePair.add(new BasicNameValuePair("attrs", attributes));
        return restUrl + "/rm/node/mbean?" + URLEncodedUtils.format(nameValuePair, "utf-8");
    }

    private String generateObjectName() {
        if (objectName != null) {
            return objectName;
        } else if (nodeSourceName != null) {
            return "ProActiveResourceManager:name=IaasMonitoring-" + nodeSourceName;
        } else if (type != null) {
            return "sigar:Type=" + type;
        } else {
            throw new RuntimeException("Cannot generate objectName");
        }
    }

    // BUILDER CONSTRUCTOR

    private MonitoringProxy(Builder builder) {

        restUrl = builder.restUrl;
        restCredentials = builder.restCredentials;
        restUsername = builder.restUsername;
        restPassword = builder.restPassword;
        nodeSourceName = builder.nodeSourceName;
        objectName = builder.objectName;
        type = builder.type;
        jmxUrl = builder.jmxUrl;
        httpClient = new DefaultHttpClient();
        if (builder.insecureAccess)
            httpClient = HttpUtility.turnClientIntoInsecure(httpClient);

    }

    // BUILDER CLASS

    public static class Builder {

        private String restUrl;
        private String restCredentials;
        private String restUsername;
        private String restPassword;
        private String nodeSourceName;
        private String objectName;
        private String type;
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

        public Builder setObjectName(String objectName) {
            this.objectName = objectName;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
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

        public Builder setCredentials(String credentials) {
            this.restCredentials = credentials;
            return this;
        }


        public MonitoringProxy build() {
            return new MonitoringProxy(this);
        }

    }
}
