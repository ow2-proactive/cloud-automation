package org.ow2.proactive.brokering.updater.requests;

public class UnknownRequest extends UpdaterRequest {

    private String errorMessage;

    public UnknownRequest() { }

    public UnknownRequest(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
