package io.zhijun.spring.boot.bind;

import io.zhijun.core.annotation.Experimental;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;



/**
 * Lightweight helpers around Spring Boot {@link Binder}.
 * <p>
 * Intended for Rose extension modules and advanced integrations that need small, direct property binding helpers
 * without introducing a larger configuration object model.
 */
@Experimental(since = "0.0.1")
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
        BindResult<T> result = binder.bind(name, Bindable.of(targetType));
        return result.orElse(defaultValue);
    }

    public <T> BindResult<T> bindResult(String name, Class<T> targetType) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(targetType, "targetType cannot be null");
        return binder.bind(name, Bindable.of(targetType));
    }

    public boolean bindBoolean(String name, boolean defaultValue) {
        return bind(name, Boolean.class, Boolean.valueOf(defaultValue)).booleanValue();
    }

    public String bindString(String name, String defaultValue) {
        return bind(name, String.class, defaultValue);
    }
}
