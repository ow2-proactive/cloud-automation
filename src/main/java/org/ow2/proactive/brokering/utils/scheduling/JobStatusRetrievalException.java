package org.ow2.proactive.brokering.utils.scheduling;

public class JobStatusRetrievalException extends Exception {
    public JobStatusRetrievalException(String message) {
        super(message);
    }

    public JobStatusRetrievalException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobStatusRetrievalException(Throwable throwable) {
        super(throwable);
    }
}
