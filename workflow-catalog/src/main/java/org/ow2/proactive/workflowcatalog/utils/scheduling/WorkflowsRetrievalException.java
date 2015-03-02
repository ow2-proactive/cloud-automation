package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class WorkflowsRetrievalException extends Exception {
    public WorkflowsRetrievalException(String message) {
        super(message);
    }

    public WorkflowsRetrievalException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public WorkflowsRetrievalException(Throwable throwable) {
        super(throwable);
    }
}
