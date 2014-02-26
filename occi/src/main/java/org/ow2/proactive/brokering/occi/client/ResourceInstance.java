package org.ow2.proactive.brokering.occi.client;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceInstance extends HashMap<String, String> {

    private static final String PREFIX = "X-OCCI-Location: ";

    private String location;

    public ResourceInstance(Map<String, String> args) {
        this.putAll(args);
    }

    public ResourceInstance(String location) {
        if (location.contains(PREFIX))
            this.location = extractLocation(location);
        else
            this.location = location.trim();
    }

    public String getLocation() {
        if (location != null) {
            return location;
        } else {
            throw new RuntimeException("No location is set");
        }
    }

    public String getCategory() {
        if (location != null) {
            return extractCategory(location);
        } else {
            throw new RuntimeException("No location is set");
        }
    }

    public String getUuid() {
        if (containsKey("occi.core.id")) {
            return get("occi.core.id");
        } else if (location != null) {
            return extractUuid(location);
        } else {
            throw new RuntimeException("No occi.core.id or location are set");
        }
    }

    private String extractCategory(String resourceLocationUrl) {
        Pattern propertyRegex = Pattern.compile("/([a-zA-Z]*)/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
        Matcher m = propertyRegex.matcher(resourceLocationUrl);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract category: " + resourceLocationUrl);
    }

    private String extractUuid(String resourceLocationUrl) {
        Pattern propertyRegex = Pattern.compile("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");
        Matcher m = propertyRegex.matcher(resourceLocationUrl);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract UUID: " + resourceLocationUrl);
    }

    private String extractLocation(String resourceLocationRaw) {
        Pattern propertyRegex = Pattern.compile(PREFIX + "(.*)");
        Matcher m = propertyRegex.matcher(resourceLocationRaw);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract location: " + resourceLocationRaw);
    }

    public String toString() {
        return "Location: " + location + " -> " + super.toString();
    }

}
