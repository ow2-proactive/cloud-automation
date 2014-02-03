package org.ow2.proactive.brokering.monitoring;

public class MonitoringException extends Exception {

    public MonitoringException(String message) {
        super(message);
    }

    public MonitoringException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MonitoringException(Throwable throwable) {
        super(throwable);
    }

}

