package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class JobCreationException extends Exception {
    public JobCreationException(String message) {
        super(message);
    }

    public JobCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobCreationException(Throwable throwable) {
        super(throwable);
    }
}
