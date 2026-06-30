package io.zhijun.spring.boot.properties.bind.util;

import io.zhijun.spring.boot.properties.bind.BindListener;
import io.zhijun.spring.boot.properties.bind.ListenableBindHandlerAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

import static io.zhijun.spring.boot.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static org.springframework.boot.context.properties.bind.BindConstructorProvider.DEFAULT;
import static org.springframework.boot.context.properties.bind.Bindable.of;
import static org.springframework.boot.context.properties.bind.Binder.get;

/**
 * 绑定工具类
 */
public abstract class BindUtils {

    public static boolean isConfigurationPropertiesBean(Bindable<?> target, BindContext context) {
        return target != null && target.getAnnotation(CONFIGURATION_PROPERTIES_CLASS) != null && isConfigurationPropertiesBean(context);
    }

    public static boolean isConfigurationPropertiesBean(BindContext context) {
        return context != null && context.getDepth() == 0;
    }

    public static boolean isBoundProperty(BindContext context) {
        return context != null && context.getDepth() > 0 && context.getConfigurationProperty() != null;
    }

    public static <T> T bind(Environment environment, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners) {
        Binder binder = get(environment);
        return bind(binder, propertyNamePrefix, targetType, bindListeners);
    }

    public static <T> T bind(Map<?, ?> properties, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners) {
        ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(propertySource);
        return bind(binder, propertyNamePrefix, targetType, bindListeners);
    }

    public static Constructor<?> getBindConstructor(Bindable<?> bindable, boolean isNestedConstructorBinding) {
        return DEFAULT.getBindConstructor(bindable, isNestedConstructorBinding);
    }

    protected static <T> T bind(Binder binder, String name, Class<T> targetType, BindListener... bindListeners) {
        ListenableBindHandlerAdapter bindHandlerAdapter = new ListenableBindHandlerAdapter(Arrays.asList(bindListeners));
        Bindable<T> bindable = of(targetType);
        BindResult<T> result = binder.bind(name, bindable, bindHandlerAdapter);
        return result.orElse(null);
    }

    private BindUtils() {
    }
}
