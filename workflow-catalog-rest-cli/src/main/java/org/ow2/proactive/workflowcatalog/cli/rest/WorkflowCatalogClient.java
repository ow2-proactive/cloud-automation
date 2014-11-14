package org.ow2.proactive.workflowcatalog.cli.rest;

import org.ow2.proactive.workflowcatalog.api.Workflows;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;


public interface WorkflowCatalogClient {

    public void init(String url, String sessionId, ClientHttpEngine httpClient) throws Exception;
    public Workflows getWorkflowsProxy();

}
