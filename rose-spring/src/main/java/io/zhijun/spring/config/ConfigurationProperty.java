package io.zhijun.spring.config;

import io.zhijun.core.annotation.Nullable;
import org.springframework.core.env.PropertySource;

import java.util.Objects;

public class ConfigurationProperty {

    private final String name;

    private Object value;

    @Nullable
    private final PropertySource<?> propertySource;

    private Class<?> type;

    private boolean required;

    private Object defaultValue;

    public ConfigurationProperty(String name) {
        this(name, null, null);
    }

    public ConfigurationProperty(String name, Object value, @Nullable PropertySource<?> propertySource) {
        this.name = name;
        this.value = value;
        this.propertySource = propertySource;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Nullable
    public PropertySource<?> getPropertySource() {
        return propertySource;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationProperty)) return false;
        ConfigurationProperty that = (ConfigurationProperty) o;
        return required == that.required
                && Objects.equals(name, that.name)
                && Objects.equals(value, that.value)
                && Objects.equals(type, that.type)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, type, required, defaultValue);
    }

    @Override
    public String toString() {
        return "ConfigurationProperty{name='" + name + "', value=" + value
                + ", type=" + type + ", required=" + required + ", defaultValue=" + defaultValue + "}";
    }

}
