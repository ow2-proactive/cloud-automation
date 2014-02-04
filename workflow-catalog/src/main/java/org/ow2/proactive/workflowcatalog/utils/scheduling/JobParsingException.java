package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class JobParsingException extends Exception {
    public JobParsingException(String message) {
        super(message);
    }

    public JobParsingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobParsingException(Throwable throwable) {
        super(throwable);
    }
}
