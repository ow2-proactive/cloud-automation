package org.ow2.proactive.workflowcatalog.api;

import java.io.IOException;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.ow2.proactive.workflowcatalog.*;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.apache.shiro.session.UnknownSessionException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

public enum Core {
    INSTANCE();

    private Core() {}

    public Collection<Workflow> getWorkflows()
            throws NotConnectedRestException, WorkflowsRetrievalException {
        ISchedulerProxy proxy = getScheduler();
        return new StudioApiCatalog(proxy).getWorkflows();
    }

    public References executeWorkflow(WorkflowParameters data)
            throws NotConnectedRestException, JobSubmissionException {
        References references = new References();
        Collection<Workflow> workflows;
        try {
            workflows = new StudioApiCatalog(getScheduler()).getWorkflows(data);
        } catch (WorkflowsRetrievalException e) {
            throw new JobSubmissionException("Cannot list workflows", e);
        }

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

    private ISchedulerProxy getScheduler() throws NotConnectedRestException {
        try {
            return SchedulerRestSession.getScheduler();
        } catch (UnknownSessionException e) {
            throw new NotConnectedRestException(e);
        }
    }    

}

