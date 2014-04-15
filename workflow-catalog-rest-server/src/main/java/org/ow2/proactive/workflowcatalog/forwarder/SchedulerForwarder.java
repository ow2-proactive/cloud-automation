package org.ow2.proactive.workflowcatalog.forwarder;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.URI;

public class SchedulerForwarder extends ProxyServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        try {
            targetUriObj = new URI(ConfigurationHelper.getConfiguration().scheduler.url);
        } catch (Exception e) {
            throw new RuntimeException("Trying to read scheduler URL: " + e, e);
        }
        targetUri = targetUriObj.toString();

        HttpParams hcParams = new BasicHttpParams();
        readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
        proxyClient = createHttpClient(hcParams);
    }
}
