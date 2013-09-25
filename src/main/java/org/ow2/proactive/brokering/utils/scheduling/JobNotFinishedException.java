package org.ow2.proactive.brokering.utils.scheduling;

public class JobNotFinishedException extends Exception {
    public JobNotFinishedException(String message) {
        super(message);
    }

    public JobNotFinishedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobNotFinishedException(Throwable throwable) {
        super(throwable);
    }
}
