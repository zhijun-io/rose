package io.zhijun.boot.context.properties.bind;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Lightweight helpers around Spring Boot {@link Binder}.
 */
public final class RoseBinder {

    private final Binder binder;

    private RoseBinder(Binder binder) {
        this.binder = binder;
    }

    public static RoseBinder get(Environment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return new RoseBinder(Binder.get(environment));
    }

    public <T> T bind(String name, Class<T> targetType, T defaultValue) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(targetType, "targetType cannot be null");
        BindResult<T> result = binder.bind(name, targetType);
        return result.orElse(defaultValue);
    }

    public boolean bindBoolean(String name, boolean defaultValue) {
        return bind(name, Boolean.class, Boolean.valueOf(defaultValue)).booleanValue();
    }

    public String bindString(String name, String defaultValue) {
        return bind(name, String.class, defaultValue);
    }
}
