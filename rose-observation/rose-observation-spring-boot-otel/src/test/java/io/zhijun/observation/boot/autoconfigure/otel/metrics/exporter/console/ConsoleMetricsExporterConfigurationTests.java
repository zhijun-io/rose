package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.console;

import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsoleMetricsExporterConfiguration}.
 */
class ConsoleMetricsExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConsoleMetricsExporterConfiguration.class));

    @Test
    void consoleExporterBeanCreatedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.exporter.type=console")
            .run(context -> {
                assertThat(context).hasSingleBean(LoggingMetricExporter.class);
                assertThat(context).hasSingleBean(ConsoleMetricsExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenOtherExporterEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.exporter.type=otlp")
            .run(context -> {
                assertThat(context).doesNotHaveBean(LoggingMetricExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleMetricsExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenNoPropertySet() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(LoggingMetricExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleMetricsExporterConfiguration.class);
            });
    }

    @Test
    void existingConsoleExporterBeanRespected() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.exporter.type=console")
            .withUserConfiguration(CustomLoggingMetricExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(LoggingMetricExporter.class);
                assertThat(context).hasSingleBean(ConsoleMetricsExporterConfiguration.class);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomLoggingMetricExporterConfiguration {

        @Bean
        LoggingMetricExporter loggingMetricExporter() {
            return LoggingMetricExporter.create();
        }

    }
}
