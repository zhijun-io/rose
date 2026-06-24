package io.zhijun.observability.otel.autoconfigure.logs.exporter.console;

import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsoleLoggingExporterConfiguration}.
 */
class ConsoleLoggingExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConsoleLoggingExporterConfiguration.class));

    @Test
    void consoleExporterBeanCreatedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=console")
            .run(context -> {
                assertThat(context).hasSingleBean(SystemOutLogRecordExporter.class);
                assertThat(context).hasSingleBean(ConsoleLoggingExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenOtherExporterEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=otlp")
            .run(context -> {
                assertThat(context).doesNotHaveBean(SystemOutLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleLoggingExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenNoPropertySet() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(SystemOutLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleLoggingExporterConfiguration.class);
            });
    }

    @Test
    void existingConsoleExporterBeanRespected() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=console")
            .withUserConfiguration(CustomSystemOutLogRecordExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SystemOutLogRecordExporter.class);
                assertThat(context).hasSingleBean(ConsoleLoggingExporterConfiguration.class);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomSystemOutLogRecordExporterConfiguration {

        @Bean
        SystemOutLogRecordExporter systemOutLogRecordExporter() {
            return SystemOutLogRecordExporter.create();
        }

    }
}
