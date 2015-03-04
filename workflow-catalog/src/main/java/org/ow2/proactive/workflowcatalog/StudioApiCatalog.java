package org.ow2.proactive.workflowcatalog;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.WorkflowsRetrievalException;
import java.util.*;

public class StudioApiCatalog implements Catalog {
    private static final Logger logger = Logger.getLogger(StudioApiCatalog.class.getName());
    private ISchedulerProxy proxy;

    public StudioApiCatalog(ISchedulerProxy proxy) {
        this.proxy = proxy;
    }

    public Collection<Workflow> getWorkflows(WorkflowParameters filter)
            throws WorkflowsRetrievalException {
        ArrayList<Workflow> result = new ArrayList<Workflow>();

        for (org.ow2.proactive_grid_cloud_portal.studio.Workflow w : proxy.listWorkflows()) {
            Workflow workflow = convert(w);
            workflow.update();
            if (filter == null || filter.matches(workflow))
                result.add(workflow);
        }

        return result;
    }

    public Collection<Workflow> getWorkflows() throws WorkflowsRetrievalException {
        return getWorkflows(null);
    }

    public void forceUpdate() {}

    private Workflow convert(org.ow2.proactive_grid_cloud_portal.studio.Workflow w) {
        return new Workflow(w.getName(), w.getXml());
    }
}

