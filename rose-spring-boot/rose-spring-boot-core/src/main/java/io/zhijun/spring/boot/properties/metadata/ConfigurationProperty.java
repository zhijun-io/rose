package io.zhijun.spring.boot.properties.metadata;

public class ConfigurationProperty {

    private final String name;

    private String type;

    private String description;

    private Object defaultValue;

    private String sourceType;

    private String sourceMethod;

    public ConfigurationProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public void setSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    @Override
    public String toString() {
        return "ConfigurationProperty{name='" + name + "', type='" + type + "'}";
    }
}
