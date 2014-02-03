package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class JobSubmissionException extends Exception {
    public JobSubmissionException(String message) {
        super(message);
    }

    public JobSubmissionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobSubmissionException(Throwable throwable) {
        super(throwable);
    }
}
