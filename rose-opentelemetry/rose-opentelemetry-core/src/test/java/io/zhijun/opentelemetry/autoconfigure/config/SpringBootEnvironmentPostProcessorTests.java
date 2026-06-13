package io.zhijun.opentelemetry.autoconfigure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SpringBootEnvironmentPostProcessor}.
 */
class SpringBootEnvironmentPostProcessorTests {

    private final SpringBootEnvironmentPostProcessor processor = new SpringBootEnvironmentPostProcessor();

    @Test
    void postProcessEnvironmentShouldThrowExceptionWhenEnvironmentIsNull() {
        assertThatThrownBy(() -> processor.postProcessEnvironment(null, new SpringApplication()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("environment cannot be null");
    }

    @Test
    void postProcessEnvironmentShouldAddPropertySourceFirst() {
        MockEnvironment environment = new MockEnvironment();
        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getPropertySources().stream().findFirst())
            .hasValueSatisfying(propertySource -> assertThat(propertySource.getName()).isEqualTo("rose-opentelemetry-spring-boot"));
    }

    @Test
    void postProcessEnvironmentShouldExcludeActuatorAutoConfigurations() {
        MockEnvironment environment = new MockEnvironment();
        processor.postProcessEnvironment(environment, new SpringApplication());

        String excludedConfigs = environment.getProperty("spring.autoconfigure.exclude");
        assertThat(excludedConfigs).contains(
            "org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration"
        );
    }

    @Test
    void postProcessEnvironmentShouldAppendToExistingExclusions() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("spring.autoconfigure.exclude", "com.example.ExistingAutoConfiguration");
        processor.postProcessEnvironment(environment, new SpringApplication());

        String excludedConfigs = environment.getProperty("spring.autoconfigure.exclude");
        assertThat(excludedConfigs)
            .startsWith("com.example.ExistingAutoConfiguration,")
            .contains(
                "org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration"
            );
    }

    @Test
    void postProcessEnvironmentShouldExcludeTracingAutoConfigurationWhenRoseOtelDisabled() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("rose.otel.enabled", "false");
        processor.postProcessEnvironment(environment, new SpringApplication());

        String excludedConfigs = environment.getProperty("spring.autoconfigure.exclude");
        assertThat(excludedConfigs).contains(
            "org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration"
        );
    }

}
