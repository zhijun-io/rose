package io.zhijun.spring.core.env.listener;

import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import io.zhijun.spring.core.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;

/**
 * Listener for environment access and property source changes.
 */
public interface EnvironmentListener extends ProfileListener, PropertyResolverListener {

    default void beforeGetPropertySources(ConfigurableEnvironment environment) {}

    default void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {}

    default void beforeGetSystemProperties(ConfigurableEnvironment environment) {}

    default void afterGetSystemProperties(ConfigurableEnvironment environment, Map<String, Object> systemProperties) {}

    default void beforeGetSystemEnvironment(ConfigurableEnvironment environment) {}

    default void afterGetSystemEnvironment(
            ConfigurableEnvironment environment, Map<String, Object> systemEnvironmentVariables) {}

    default void beforeMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {}

    default void afterMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {}

    default void onPropertySourceChanged(PropertySourceChangedEvent event) {}

    default void onPropertySourcesChanged(PropertySourcesChangedEvent event) {}
}
