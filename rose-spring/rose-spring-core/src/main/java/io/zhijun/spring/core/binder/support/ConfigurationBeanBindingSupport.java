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
 * Shared rules for {@link EnableConfigurationBeanBinding} registration and env hot-reload.
 * <p>
 * Keeps {@link io.zhijun.spring.core.binder.annotation.ConfigurationBeanBindingRegistrar} and
 * {@link io.zhijun.spring.core.binder.refresh.ConfigurationBeanBindingRefreshable} aligned on
 * how prefix properties are sliced, especially when {@code multiple = true}.
 */
public final class ConfigurationBeanBindingSupport {

    /** {@link BeanDefinition#getSource()} marker for configuration beans. */
    public static final Class<?> CONFIGURATION_BEAN_SOURCE = EnableConfigurationBeanBinding.class;

    /** Resolved annotation {@code prefix} stored at registration time. */
    public static final String CONFIGURATION_BINDING_PREFIX = "configurationBindingPrefix";

    /** Annotation {@code multiple} flag stored at registration time. */
    public static final String CONFIGURATION_BINDING_MULTIPLE = "configurationBindingMultiple";

    private ConfigurationBeanBindingSupport() {
    }

    public static boolean isConfigurationBeanDefinition(BeanDefinition beanDefinition) {
        return beanDefinition != null && CONFIGURATION_BEAN_SOURCE.equals(beanDefinition.getSource());
    }

    /**
     * Returns whether any changed key belongs to the binding prefix
     * ({@code key == prefix} or {@code key.startsWith(prefix + ".")}).
     */
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

    /**
     * When {@code multiple} is false, returns the full map; otherwise extracts the subtree for
     * {@code beanName} (first-level segment under the annotation prefix).
     */
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

    /** Reads current {@code prefix.*} from {@code environment} and applies {@link #resolveSubProperties}. */
    public static Map<String, Object> resolveBindingProperties(ConfigurableEnvironment environment, String prefix,
            boolean multiple, String beanName) {
        Map<String, Object> configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix);
        return resolveSubProperties(multiple, beanName, configurationProperties, environment);
    }
}
