package io.zhijun.spring.core.binder.support;

import java.util.Map;
import java.util.Set;

import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding;
import io.zhijun.spring.core.env.PropertySourcesUtils;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * Shared helpers for configuration bean registration and refresh.
 */
public final class ConfigurationBeanBindingSupport {

    public static final Class<?> CONFIGURATION_BEAN_SOURCE = EnableConfigurationBeanBinding.class;

    public static final String CONFIGURATION_BINDING_PREFIX = "configurationBindingPrefix";

    public static final String CONFIGURATION_BINDING_MULTIPLE = "configurationBindingMultiple";

    private ConfigurationBeanBindingSupport() {
    }

    public static boolean isConfigurationBeanDefinition(BeanDefinition beanDefinition) {
        return beanDefinition != null && CONFIGURATION_BEAN_SOURCE.equals(beanDefinition.getSource());
    }

    public static boolean prefixAffected(String prefix, Set<String> changedKeys) {
        if (prefix == null || changedKeys == null || changedKeys.isEmpty()) {
            return false;
        }
        for (String key : changedKeys) {
            if (key.equals(prefix) || key.startsWith(prefix + ".")) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> resolveSubProperties(boolean multiple, String beanName,
            Map<String, Object> configurationProperties, Environment environment) {
        if (!multiple) {
            return configurationProperties;
        }
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addLast(new MapPropertySource("_", configurationProperties));
        return PropertySourcesUtils.getSubProperties(propertySources, environment,
                PropertySourcesUtils.normalizePrefix(beanName));
    }

    public static Map<String, Object> resolveBindingProperties(ConfigurableEnvironment environment, String prefix,
            boolean multiple, String beanName) {
        Map<String, Object> configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix);
        return resolveSubProperties(multiple, beanName, configurationProperties, environment);
    }
}
