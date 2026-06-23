package io.zhijun.opentelemetry.autoconfigure.metrics.exporter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.opentelemetry.autoconfigure.exporter.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.console.ConsoleMetricsExporterConfiguration;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.otlp.OtlpMetricsExporterConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryMetricsExporterAutoConfiguration}.
 */
class OpenTelemetryMetricsExporterAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
                OpenTelemetryMetricsExporterAutoConfiguration.class))
            .withUserConfiguration(io.zhijun.opentelemetry.autoconfigure.support.MetricsTestBeans.class);

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(OtlpMetricsExporterConfiguration.class));
    }

    @Test
    void autoConfigurationNotActivatedWhenMetricsDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(OtlpMetricsExporterConfiguration.class));
    }

    @Test
    void configurationPropertiesEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenTelemetryMetricsExporterProperties.class);
            assertThat(context).hasSingleBean(OpenTelemetryMetricsExporterAutoConfiguration.class);
        });
    }

    @Test
    void consoleExporterConfigurationImportedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.exporter.type=console")
            .run(context -> {
                assertThat(context).hasSingleBean(ConsoleMetricsExporterConfiguration.class);
                assertThat(context).doesNotHaveBean(OtlpMetricsExporterConfiguration.class);
                assertThat(context).hasSingleBean(OpenTelemetryMeterProviderBuilderCustomizer.class);
            });
    }

    @Test
    void otlpExporterConfigurationImportedWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(ConsoleMetricsExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
        });
    }

    @Test
    void otlpExporterConfigurationImportedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.exporter.type=otlp")
            .run(context -> {
                assertThat(context).doesNotHaveBean(ConsoleMetricsExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
            });
    }

}
