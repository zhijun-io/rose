package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.ConfigurablePropertyResolver;

import io.zhijun.spring.core.env.listener.PropertyResolverListener;

public class FactoryLoadedPropertyResolverListener implements PropertyResolverListener {

    private static final List<String> CALLBACKS = new ArrayList<String>();

    public static void reset() {
        CALLBACKS.clear();
    }

    public static List<String> callbacks() {
        return new ArrayList<String>(CALLBACKS);
    }

    @Override
    public void beforeGetProperty(
            ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object defaultValue) {
        CALLBACKS.add("beforeGetProperty");
    }

    @Override
    public void afterGetProperty(
            ConfigurablePropertyResolver propertyResolver,
            String name,
            Class<?> targetType,
            Object value,
            Object defaultValue) {
        CALLBACKS.add("afterGetProperty");
    }
}
