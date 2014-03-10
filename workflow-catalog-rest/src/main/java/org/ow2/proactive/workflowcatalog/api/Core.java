package org.ow2.proactive.workflowcatalog.api;

import java.io.IOException;
import java.util.*;
import org.apache.http.auth.AuthenticationException;
import org.ow2.proactive.workflowcatalog.*;
import org.ow2.proactive.workflowcatalog.utils.scheduling.*;

import javax.xml.transform.TransformerException;

import static org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper.*;

public class Core {

    private static Core instance;
    public static Core getInstance() {
        if (instance == null)
            instance = new Core();
        return instance;
    }

    private SchedulerProxy scheduler;
    private Catalog catalog;

    private Core() {
        Configuration config = getConfiguration();
        SchedulerLoginData loginData = getSchedulerLoginData(config);
        scheduler = new SchedulerProxy(loginData);
        catalog = new Catalog(getCatalogPath(config), config.catalog.refresh * 1000);
    }

    public Collection<Workflow> getWorkflows() {
        return catalog.getWorkflows();
    }

    public References executeWorkflow(WorkflowParameters data) throws JobSubmissionException {
        References references = new References();
        Collection<Workflow> workflows = catalog.getWorkflows(data);
        for (Workflow w: workflows) {
            try {
                JobSubmissionResponse jsonResponse = scheduler.submitJob(w.configure(data.getVariables()));
                references.add(Reference.buildJobReference(w.getName(), jsonResponse));
            } catch (JobParsingException e) {
                throw new JobSubmissionException("Error parsing", e);
            } catch (AuthenticationException e) {
                throw new JobSubmissionException("Error authenticating", e);
            } catch (TransformerException e) {
                throw new JobSubmissionException("Unexpected error", e);
            } catch (IOException e) {
                throw new JobSubmissionException(e);
            }
        }

        return references;
    }

}

