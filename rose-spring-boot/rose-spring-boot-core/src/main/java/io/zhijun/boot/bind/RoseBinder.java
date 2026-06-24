package io.zhijun.boot.bind;

import java.util.Arrays;

import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Lightweight helpers around Spring Boot {@link Binder}.
 */
public final class RoseBinder {

    private final Binder binder;

    private final BindHandler bindHandler;

    private RoseBinder(Binder binder, BindHandler bindHandler) {
        this.binder = binder;
        this.bindHandler = bindHandler;
    }

    public static RoseBinder get(Environment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return new RoseBinder(Binder.get(environment), null);
    }

    public static RoseBinder get(Environment environment, RoseBindListener... listeners) {
        Assert.notNull(environment, "environment cannot be null");
        if (listeners == null || listeners.length == 0) {
            return get(environment);
        }
        BindHandler handler = new ListenableBindHandlerAdapter(Arrays.asList(listeners));
        return new RoseBinder(Binder.get(environment), handler);
    }

    public RoseBinder withListeners(RoseBindListener... listeners) {
        if (listeners == null || listeners.length == 0) {
            return this;
        }
        return new RoseBinder(binder, new ListenableBindHandlerAdapter(Arrays.asList(listeners)));
    }

    public <T> T bind(String name, Class<T> targetType, T defaultValue) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(targetType, "targetType cannot be null");
        BindResult<T> result = bind(name, Bindable.of(targetType));
        return result.orElse(defaultValue);
    }

    public <T> BindResult<T> bindResult(String name, Class<T> targetType) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(targetType, "targetType cannot be null");
        return bind(name, Bindable.of(targetType));
    }

    public boolean bindBoolean(String name, boolean defaultValue) {
        return bind(name, Boolean.class, Boolean.valueOf(defaultValue)).booleanValue();
    }

    public String bindString(String name, String defaultValue) {
        return bind(name, String.class, defaultValue);
    }

    private <T> BindResult<T> bind(String name, Bindable<T> target) {
        if (bindHandler == null) {
            return binder.bind(name, target);
        }
        return binder.bind(name, target, bindHandler);
    }
}
