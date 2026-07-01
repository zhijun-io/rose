package io.zhijun.spring.core.env;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

/**
 * 监听 {@link ConfigurableEnvironment} 的操作，包含 {@link ProfileListener} 和 {@link PropertyResolverListener}。
 *
 * @see ProfileListener
 * @see PropertyResolverListener
 */
public interface EnvironmentListener extends ProfileListener, PropertyResolverListener {

    default void beforeGetPropertySources(ConfigurableEnvironment environment) {
    }

    default void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
    }

    default void beforeGetSystemProperties(ConfigurableEnvironment environment) {
    }

    default void afterGetSystemProperties(ConfigurableEnvironment environment, Map<String, Object> systemProperties) {
    }

    default void beforeGetSystemEnvironment(ConfigurableEnvironment environment) {
    }

    default void afterGetSystemEnvironment(ConfigurableEnvironment environment, Map<String, Object> systemEnvironmentVariables) {
    }

    default void beforeMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
    }

    default void afterMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
    }
}
