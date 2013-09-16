package org.ow2.proactive.brokering.monitoring;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.utils.HttpUtility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MonitoringProxy {

    private static final boolean INSECURE_ACCESS = true;

    private static final Logger logger = Logger.getLogger(MonitoringProxy.class.getName());

    private String restUrl;
    private String restUsername;
    private String restPassword;
    private String nodeSourceName;
    private String jmxUrl;
    private HttpClient httpClient;

    public String getAttribute(String attributes) throws MonitoringException {
        String sessid;
        try {
            sessid = connectToRestRm(restUrl, restUsername, restPassword);
        } catch (AuthenticationException e) {
            throw new MonitoringException(e);
        }
        String output = getAttribute(attributes, sessid);
        disconnectFromRestRm(sessid);
        return output;
    }

    public String getAttribute(String attributes, String sessid) throws MonitoringException {
        HttpGet get = generateMonitoringHttpGet(attributes, sessid);
        try {
            HttpResponse response = httpClient.execute(get);
            return getResult(response);
        } catch (IOException e) {
            throw new MonitoringException("Cannot get monitoring information", e);
        }

    }

    private String generateMonitoringUri(String attributes) {
        ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("nodejmxurl", jmxUrl));
        nameValuePair.add(new BasicNameValuePair("objectname", generateObjectName(nodeSourceName)));
        nameValuePair.add(new BasicNameValuePair("attrs", attributes));
        return restUrl + "/rm/node/mbean?" + URLEncodedUtils.format(nameValuePair, "utf-8");
    }

    public String connectToRestRm(String restapi, String username, String password) throws AuthenticationException {
        try {
            HttpPost post = generateLoginHttpPost(restapi, username, password);
            HttpResponse response = httpClient.execute(post);
            return getResult(response);
        } catch (IOException e) {
            throw new AuthenticationException("Failed authenticating to the Rest API", e);
        }
    }

    public Integer disconnectFromRestRm(String sessid) throws MonitoringException {
        try {
            HttpPost post = generateDisconnectLoginHttpPost(sessid);
            HttpResponse response = httpClient.execute(post);
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new MonitoringException("Failed disconnecting from Rest API", e);
        }
    }

    private String generateObjectName(String nodeSourceName) {
        return "ProActiveResourceManager:name=IaasMonitoring-" + nodeSourceName;
    }

    private HttpGet generateMonitoringHttpGet(String attributes, String sessid) {
        String endpoint = generateMonitoringUri(attributes);
        HttpGet get = new HttpGet(endpoint);
        get.addHeader("sessionid", sessid);
        return get;
    }

    private HttpPost generateLoginHttpPost(String restapi, String username, String password)
            throws UnsupportedEncodingException {
        String endpoint = restapi + "/rm/login";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("Content-type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity("username=" + username + "&password=" + password, "UTF-8"));
        return post;
    }

    private HttpPost generateDisconnectLoginHttpPost(String sessid)
            throws UnsupportedEncodingException {
        String endpoint = restUrl + "/rm/disconnect";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("sessionid", sessid);
        return post;
    }

    private String getResult(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    public void setJmxUrl(String jmxUrl) {
        this.jmxUrl = jmxUrl;
    }

    public void setNodeSourceName(String nodeSourceName) {
        this.nodeSourceName = nodeSourceName;
    }

    private MonitoringProxy(Builder builder) {
        httpClient = new DefaultHttpClient();
        if (INSECURE_ACCESS)
            HttpUtility.setInsecureAccess(httpClient);
        restUrl = builder.restUrl;
        restUsername = builder.restUsername;
        restPassword = builder.restPassword;
        nodeSourceName = builder.nodeSourceName;
        jmxUrl = builder.jmxUrl;
    }

    public static class Builder {
        private String restUrl;
        private String restUsername;
        private String restPassword;
        private String nodeSourceName;
        private String jmxUrl;

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

        public MonitoringProxy build() {
            return new MonitoringProxy(this);
        }
    }
}
