package io.zhijun.spring.core.env.listener;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Listener for environment profile operations.
 */
public interface ProfileListener {

    default void beforeGetActiveProfiles(Environment environment) {}

    default void afterGetActiveProfiles(Environment environment, String[] activeProfiles) {}

    default void beforeGetDefaultProfiles(Environment environment) {}

    default void afterGetDefaultProfiles(Environment environment, String[] defaultProfiles) {}

    default void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {}

    default void afterSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {}

    default void beforeAddActiveProfile(ConfigurableEnvironment environment, String profile) {}

    default void afterAddActiveProfile(ConfigurableEnvironment environment, String profile) {}

    default void beforeSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {}

    default void afterSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {}
}
