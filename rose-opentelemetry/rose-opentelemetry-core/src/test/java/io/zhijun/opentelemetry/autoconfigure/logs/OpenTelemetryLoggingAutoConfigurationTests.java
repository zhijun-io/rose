package io.zhijun.opentelemetry.autoconfigure.logs;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.zhijun.opentelemetry.autoconfigure.support.OtelExporterMocks;

/**
 * Unit tests for {@link OpenTelemetryLoggingAutoConfiguration}.
 */
class OpenTelemetryLoggingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryLoggingAutoConfiguration.class))
            .withPropertyValues("rose.otel.enabled=true")
            .withUserConfiguration(ClockResourceConfiguration.class);

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SdkLoggerProvider.class));
    }

    @Test
    void autoConfigurationNotActivatedWhenLogsDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.logs.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SdkLoggerProvider.class));
    }

    @Test
    void batchProcessorNotCreatedWithoutExporter() {
        contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(BatchLogRecordProcessor.class));
    }

    @Test
    void loggerProviderAvailableWithDefaultConfiguration() {
        contextRunner
            .withUserConfiguration(DefaultLogRecordExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SdkLoggerProvider.class);
                assertThat(context).hasSingleBean(LogLimits.class);
                assertThat(context).hasSingleBean(BatchLogRecordProcessor.class);
            });
    }

    @Test
    void customLogLimitsConfigurationApplied() {
        contextRunner
            .withUserConfiguration(DefaultLogRecordExporterConfiguration.class)
            .withPropertyValues(
                "rose.otel.logs.limits.max-attribute-value-length=100",
                "rose.otel.logs.limits.max-number-of-attributes=50"
            )
            .run(context -> {
                LogLimits logLimits = context.getBean(LogLimits.class);
                assertThat(logLimits.getMaxAttributeValueLength()).isEqualTo(100);
                assertThat(logLimits.getMaxNumberOfAttributes()).isEqualTo(50);
            });
    }

    @Test
    void customBatchProcessorConfigurationApplied() {
        contextRunner
            .withUserConfiguration(DefaultLogRecordExporterConfiguration.class, MeterProviderConfiguration.class)
            .withPropertyValues(
                "rose.otel.logs.processor.export-timeout=10s",
                "rose.otel.logs.processor.schedule-delay=5s",
                "rose.otel.logs.processor.max-export-batch-size=512",
                "rose.otel.logs.processor.max-queue-size=2048",
                "rose.otel.logs.processor.metrics=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(BatchLogRecordProcessor.class);
            });
    }

    @Test
    void customLoggerProviderBuilderCustomizerApplied() {
        contextRunner
            .withUserConfiguration(DefaultLogRecordExporterConfiguration.class, CustomLoggerProviderConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SdkLoggerProvider.class);
                assertThat(context).hasSingleBean(OpenTelemetryLoggerProviderBuilderCustomizer.class);
            });
    }

    @Test
    void customLogRecordProcessorTakesPrecedence() {
        contextRunner
            .withUserConfiguration(DefaultLogRecordExporterConfiguration.class, CustomLogRecordProcessorConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(LogRecordProcessor.class);
                assertThat(context.getBean(LogRecordProcessor.class))
                    .isSameAs(context.getBean(CustomLogRecordProcessorConfiguration.class).customLogRecordProcessor());
            });
    }

    @Test
    void customLogRecordExporterAvailable() {
        contextRunner
            .withUserConfiguration(CustomLogRecordExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(LogRecordExporter.class);
                assertThat(context).hasSingleBean(BatchLogRecordProcessor.class);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class ClockResourceConfiguration {

        @Bean
        Clock clock() {
            return Clock.getDefault();
        }

        @Bean
        Resource resource() {
            return Resource.empty();
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class DefaultLogRecordExporterConfiguration {

        @Bean
        LogRecordExporter logRecordExporter() {
            return SystemOutLogRecordExporter.create();
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class MeterProviderConfiguration {

        @Bean
        MeterProvider meterProvider() {
            return mock(MeterProvider.class);
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomLoggerProviderConfiguration {

        private final OpenTelemetryLoggerProviderBuilderCustomizer customizer =
                mock(OpenTelemetryLoggerProviderBuilderCustomizer.class);

        @Bean
        OpenTelemetryLoggerProviderBuilderCustomizer customLoggerProviderBuilderCustomizer() {
            return customizer;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomLogRecordProcessorConfiguration {

        private final BatchLogRecordProcessor customLogRecordProcessor = mock(BatchLogRecordProcessor.class);

        @Bean
        BatchLogRecordProcessor customLogRecordProcessor() {
            return customLogRecordProcessor;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomLogRecordExporterConfiguration {

        private final LogRecordExporter customLogRecordExporter = OtelExporterMocks.logRecordExporter();

        @Bean
        LogRecordExporter customLogRecordExporter() {
            return customLogRecordExporter;
        }

    }

}
