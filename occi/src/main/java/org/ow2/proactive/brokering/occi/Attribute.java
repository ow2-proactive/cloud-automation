package org.ow2.proactive.brokering.occi;

public class Attribute {
    private String name;
    private boolean isMutable;
    private boolean isRequired;
    private String defaultValue;
    private String description;
    private String value;

    public Attribute(String name, boolean mutable, boolean required, String defaultValue, String description) {
        this.name = name;
        isMutable = mutable;
        isRequired = required;
        this.defaultValue = defaultValue;
        this.description = description;
        this.value = null;
    }

    public Attribute(String name, boolean mutable, boolean required, String defaultValue) {
        this(name, mutable, required, defaultValue, "");
    }

    public Attribute(String name, boolean mutable, boolean required) {
        this(name, mutable, required, "", "");
    }

    public String getName() {
        return name;
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (isMutable() || this.value == null) {
            this.value = value.replaceAll("\"","");
        }
    }

    public String toString() {
//        return name + "=" + value;
        return value;
    }
}
