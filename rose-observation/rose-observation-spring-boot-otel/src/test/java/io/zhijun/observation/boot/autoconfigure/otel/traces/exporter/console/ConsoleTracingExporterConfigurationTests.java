package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.console;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsoleTracingExporterConfiguration}.
 */
class ConsoleTracingExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConsoleTracingExporterConfiguration.class));

    @Test
    void consoleExporterBeanCreatedWhenEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.type=console")
            .run(context -> {
                assertThat(context).hasSingleBean(LoggingSpanExporter.class);
                assertThat(context).hasSingleBean(ConsoleTracingExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenOtherExporterEnabled() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.type=otlp")
            .run(context -> {
                assertThat(context).doesNotHaveBean(LoggingSpanExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleTracingExporterConfiguration.class);
            });
    }

    @Test
    void consoleExporterBeanNotCreatedWhenNoPropertySet() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(LoggingSpanExporter.class);
                assertThat(context).doesNotHaveBean(ConsoleTracingExporterConfiguration.class);
            });
    }

    @Test
    void existingConsoleExporterBeanRespected() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.type=console")
            .withUserConfiguration(CustomLoggingSpanExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(LoggingSpanExporter.class);
                assertThat(context).hasSingleBean(ConsoleTracingExporterConfiguration.class);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomLoggingSpanExporterConfiguration {

        @Bean
        LoggingSpanExporter loggingSpanExporter() {
            return LoggingSpanExporter.create();
        }

    }
}
