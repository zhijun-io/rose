package io.zhijun.spring.context;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Test implementation of {@link ConfigurableApplicationContextInitializer}.
 */
public class TestConfigurableApplicationContextInitializer
        extends ConfigurableApplicationContextInitializer {

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        // no-op for tests
    }
}
