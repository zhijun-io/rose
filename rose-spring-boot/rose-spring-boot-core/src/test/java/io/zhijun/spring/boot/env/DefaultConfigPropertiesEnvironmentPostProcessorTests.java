package io.zhijun.spring.boot.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class DefaultConfigPropertiesEnvironmentPostProcessorTests {

    private final DefaultConfigPropertiesEnvironmentPostProcessor processor =
            new DefaultConfigPropertiesEnvironmentPostProcessor();

    @Test
    void shouldLoadRoseDefaultPropertiesIntoEnvironment() {
        MockEnvironment environment = new MockEnvironment();

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(environment.getProperty("spring.lifecycle.timeout-per-shutdown-phase"))
                .isEqualTo("60s");
    }

    @Test
    void shouldNotLoadWhenDisabled() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(DefaultConfigPropertiesEnvironmentPostProcessor.ENABLED_PROPERTY, "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("server.shutdown")).isNull();
    }

    @Test
    void shouldAllowHigherPrioritySourcesToOverrideDefaults() {
        MockEnvironment environment = new MockEnvironment();
        environment
                .getPropertySources()
                .addFirst(
                        new MapPropertySource("application", Collections.singletonMap("server.shutdown", "immediate")));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("server.shutdown")).isEqualTo("immediate");
    }
}
