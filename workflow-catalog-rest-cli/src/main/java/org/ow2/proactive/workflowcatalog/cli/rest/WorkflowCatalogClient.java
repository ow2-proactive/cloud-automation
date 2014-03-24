package org.ow2.proactive.workflowcatalog.cli.rest;

import org.ow2.proactive.workflowcatalog.api.Workflows;


public interface WorkflowCatalogClient {

    public void init(String url, String login, String password) throws Exception;
    public void init(String url, String sessionId) throws Exception;
    public Workflows getProxy();

}
