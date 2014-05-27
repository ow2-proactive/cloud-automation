package org.ow2.proactive.brokering.updater.requests;

import org.ow2.proactive.brokering.occi.categories.Utils;

import javax.json.JsonObject;
import java.util.Map;

public class CreateInstanceRequest extends UpdaterRequest {

    private String category;
    private String action;
    private String update;
    private Map<String, String> attributes;

    public CreateInstanceRequest() {}

    public CreateInstanceRequest(JsonObject data) {
        category = getString(data, CATEGORY_KEY);
        action = getString(data, ACTION_KEY);
        update = getString(data, PARENT_UPDATE_LOCATION_KEY);
        attributes = Utils.convertToMap(getJsonObject(data, ATTRIBUTES_KEY));
    }

    private String getString(JsonObject obj, String key) {
        checkKeyExists(obj, key);
        try {
            return obj.getString(key);
        } catch (ClassCastException e) {
            return Integer.toString(obj.getInt(key));
        }
    }

    private JsonObject getJsonObject(JsonObject obj, String key) {
        checkKeyExists(obj, key);
        return obj.getJsonObject(key);
    }

    private void checkKeyExists(JsonObject obj, String key) {
        if (!obj.containsKey(key))
            throw new RuntimeException(
                "Could not find key '" + key + "' in JSON '" + obj.toString() + "'");
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getCategory() {
        return category;
    }

    public String getAttributeToUpdateWithLocation() {
        return update;
    }

    public void processSpecialAttributes(String locationUrl) {
        for (String key: attributes.keySet()) {
            String value = attributes.get(key);
            if (UpdaterRequest.LOCATION_OF_PARENT_KEY.equals(value)) {
                attributes.put(key, locationUrl);
            }
        }
    }

}
