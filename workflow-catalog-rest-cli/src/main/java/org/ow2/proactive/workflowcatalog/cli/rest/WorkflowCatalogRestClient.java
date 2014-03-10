package org.ow2.proactive.workflowcatalog.cli.rest;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.ow2.proactive.workflowcatalog.api.RestApi;

public class WorkflowCatalogRestClient extends ClientBase implements WorkflowCatalogClient {

    private RestApi proxy;

    private WorkflowCatalogRestClient() {}

    public static WorkflowCatalogClient createInstance() {
        return new WorkflowCatalogRestClient();
    }

    public void init(String url, String login, String password) throws Exception {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(url);
        proxy = target.proxy(RestApi.class);
    }

    @Override
    public void init(String url, String sessionId) throws Exception {
        init(url, null, null);
    }

    public RestApi getProxy() {
        return proxy;
    }

}
