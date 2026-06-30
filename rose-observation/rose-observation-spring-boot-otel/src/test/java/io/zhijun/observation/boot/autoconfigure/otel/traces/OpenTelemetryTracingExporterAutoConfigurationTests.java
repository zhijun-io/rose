package io.zhijun.observation.boot.autoconfigure.otel.traces;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OtlpTracingExporterConfiguration;

/**
 * Unit test for {@link OpenTelemetryTracingExporterAutoConfiguration}.
 */
class OpenTelemetryTracingExporterAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OpenTelemetryExporterAutoConfiguration.class, OpenTelemetryTracingExporterAutoConfiguration.class));

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(OpenTelemetryTracingExporterAutoConfiguration.class));
    }

    @Test
    void autoConfigurationNotActivatedWhenTracingDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.traces.enabled=false")
                .withPropertyValues("management.tracing.enabled=true")
                .run(context ->
                        assertThat(context).doesNotHaveBean(OpenTelemetryTracingExporterAutoConfiguration.class));
    }

    @Test
    void configurationPropertiesEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenTelemetryTracingExporterProperties.class);
            assertThat(context).hasSingleBean(OpenTelemetryTracingExporterAutoConfiguration.class);
        });
    }

    @Test
    void consoleExporterConfigurationImportedWhenEnabled() {
        contextRunner
                .withPropertyValues("rose.otel.traces.exporter.type=console")
                .run(context -> {
                    assertThat(context).hasSingleBean(ConsoleTracingExporterConfiguration.class);
                    assertThat(context).doesNotHaveBean(OtlpTracingExporterConfiguration.class);
                });
    }

    @Test
    void otlpExporterConfigurationImportedWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(ConsoleTracingExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpTracingExporterConfiguration.class);
        });
    }

    @Test
    void otlpExporterConfigurationImportedWhenEnabled() {
        contextRunner.withPropertyValues("rose.otel.traces.exporter.type=otlp").run(context -> {
            assertThat(context).doesNotHaveBean(ConsoleTracingExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpTracingExporterConfiguration.class);
        });
    }
}
