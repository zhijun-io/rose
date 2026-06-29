package io.zhijun.test.boot;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Shared helpers for Spring Boot auto-configuration slice tests.
 */
public final class ApplicationContextTestSupport {

    private ApplicationContextTestSupport() {}

    /**
     * Returns a runner preconfigured with the given auto-configuration classes.
     */
    public static ApplicationContextRunner autoConfiguration(Class<?>... autoConfigurationClasses) {
        return new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(autoConfigurationClasses));
    }
}
