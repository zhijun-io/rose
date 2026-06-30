package io.zhijun.spring.boot.properties.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link ConfigurationProperties} 工具类
 */
public abstract class ConfigurationPropertiesUtils {

    public static final Class<ConfigurationProperties> CONFIGURATION_PROPERTIES_CLASS = ConfigurationProperties.class;

    public static ConfigurationProperties findConfigurationProperties(Bindable bindable) {
        ConfigurationProperties configurationProperties = (ConfigurationProperties) bindable.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        if (configurationProperties == null) {
            ResolvableType type = bindable.getType();
            Class<?> bindableType = type.resolve();
            if (bindableType != null) {
                configurationProperties = AnnotationUtils.findAnnotation(bindableType, CONFIGURATION_PROPERTIES_CLASS);
            }
        }
        return configurationProperties;
    }

    private ConfigurationPropertiesUtils() {
    }
}
