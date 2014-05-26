package org.ow2.proactive.brokering.updater.requests;

public class UpdateAttributeRequest extends UpdaterRequest {

    private String key;
    private String value;

    public UpdateAttributeRequest() { }

    public UpdateAttributeRequest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
