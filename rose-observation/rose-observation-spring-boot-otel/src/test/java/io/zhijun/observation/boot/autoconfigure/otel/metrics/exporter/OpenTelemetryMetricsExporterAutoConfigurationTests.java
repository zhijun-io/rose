package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsExporterConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.support.MetricsTestBeans;

/**
 * Unit test for {@link OpenTelemetryMetricsExporterAutoConfiguration}.
 */
class OpenTelemetryMetricsExporterAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OpenTelemetryExporterAutoConfiguration.class, OpenTelemetryMetricsExporterAutoConfiguration.class))
            .withUserConfiguration(MetricsTestBeans.class);

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
        contextRunner.withPropertyValues("rose.otel.metrics.exporter.type=otlp").run(context -> {
            assertThat(context).doesNotHaveBean(ConsoleMetricsExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
        });
    }
}
