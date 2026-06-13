package io.zhijun.opentelemetry.autoconfigure.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryEnvironmentPostProcessorTests {

    @Test
    void shouldMapOtelEnvironmentVariablesToRoseProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.sdk.disabled", "false")
                .withProperty("otel.service.name", "rose-app")
                .withProperty("otel.traces.exporter", "otlp")
                .withProperty("otel.exporter.otlp.endpoint", "http://localhost:4318");

        OpenTelemetryEnvironmentPostProcessor processor = new OpenTelemetryEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("rose.otel.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("rose.otel.resource.service-name")).isEqualTo("rose-app");
        assertThat(environment.getProperty("rose.otel.traces.exporter.type")).isEqualTo("OTLP");
        assertThat(environment.getProperty("rose.otel.exporter.otlp.endpoint")).isEqualTo("http://localhost:4318");
    }

    @Test
    void shouldSkipMappingWhenCompatibilityDisabled() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(OpenTelemetryEnvironmentPostProcessor.COMPATIBILITY_PROPERTY, "false")
                .withProperty("otel.service.name", "ignored");

        OpenTelemetryEnvironmentPostProcessor processor = new OpenTelemetryEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("rose.otel.resource.service-name")).isNull();
    }
}
