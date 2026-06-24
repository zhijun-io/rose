package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.console.ConsoleLoggingExporterConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp.OtlpLoggingExporterConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryLoggingExporterAutoConfiguration}.
 */
class OpenTelemetryLoggingExporterAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
                OpenTelemetryLoggingExporterAutoConfiguration.class));

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(OpenTelemetryLoggingExporterAutoConfiguration.class));
    }

    @Test
    void autoConfigurationNotActivatedWhenLogsDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(OpenTelemetryLoggingExporterAutoConfiguration.class));
    }

    @Test
    void configurationPropertiesEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenTelemetryLoggingExporterProperties.class);
            assertThat(context).hasSingleBean(OpenTelemetryLoggingExporterAutoConfiguration.class);
        });
    }

    @Test
    void consoleExporterConfigurationImportedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=console")
            .run(context -> {
                assertThat(context).hasSingleBean(ConsoleLoggingExporterConfiguration.class);
                assertThat(context).doesNotHaveBean(OtlpLoggingExporterConfiguration.class);
            });
    }

    @Test
    void otlpExporterConfigurationImportedWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(ConsoleLoggingExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpLoggingExporterConfiguration.class);
        });
    }

    @Test
    void otlpExporterConfigurationImportedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=otlp")
            .run(context -> {
                assertThat(context).doesNotHaveBean(ConsoleLoggingExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpLoggingExporterConfiguration.class);
            });
    }

}
