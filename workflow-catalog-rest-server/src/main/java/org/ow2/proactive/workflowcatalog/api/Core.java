package org.ow2.proactive.workflowcatalog.api;

import java.io.IOException;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.ow2.proactive.workflowcatalog.*;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

import static org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper.getCatalogPath;
import static org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper.getConfiguration;

public enum Core {
    INSTANCE();

    private Catalog catalog;

    private Core() {
        Configuration config = getConfiguration();
        catalog = new StudioApiCatalog(getScheduler());
    }

    public Collection<Workflow> getWorkflows() {
        return catalog.getWorkflows();
    }

    public References executeWorkflow(WorkflowParameters data) throws JobSubmissionException {
        References references = new References();
        Collection<Workflow> workflows = catalog.getWorkflows(data);
        for (Workflow w: workflows) {
            try {
                JobIdData jsonResponse = getScheduler().submitJob(w.configure(data.getVariables()));
                references.add(Reference.buildJobReference(w.getName(), jsonResponse));
            } catch (JobCreationException e) {
                throw new JobSubmissionException("Error creating job", e);
            } catch (JobParsingException e) {
                throw new JobSubmissionException("Error parsing job", e);
            } catch (TransformerException e) {
                throw new JobSubmissionException("Unexpected error", e);
            } catch (IOException e) {
                throw new JobSubmissionException(e);
            }
        }

        return references;
    }

    private ISchedulerProxy getScheduler() {
        return SchedulerRestSession.getScheduler();
    }    

}

