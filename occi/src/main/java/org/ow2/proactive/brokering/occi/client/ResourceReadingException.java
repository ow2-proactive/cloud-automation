package org.ow2.proactive.brokering.occi.client;

public class ResourceReadingException extends Exception {

    public ResourceReadingException(String message) {
        super(message);
    }

    public ResourceReadingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ResourceReadingException(Throwable throwable) {
        super(throwable);
    }

}
