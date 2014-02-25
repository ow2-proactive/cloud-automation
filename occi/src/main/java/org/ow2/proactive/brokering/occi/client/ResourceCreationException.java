package org.ow2.proactive.brokering.occi.client;

public class ResourceCreationException extends Exception {

    public ResourceCreationException(String message) {
        super(message);
    }

    public ResourceCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ResourceCreationException(Throwable throwable) {
        super(throwable);
    }

}
