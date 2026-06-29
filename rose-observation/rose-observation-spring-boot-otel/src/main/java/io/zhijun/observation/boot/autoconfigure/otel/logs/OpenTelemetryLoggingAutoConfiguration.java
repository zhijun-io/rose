package io.zhijun.observation.boot.autoconfigure.otel.logs;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for OpenTelemetry logging.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryLogging
@EnableConfigurationProperties(OpenTelemetryLoggingProperties.class)
public final class OpenTelemetryLoggingAutoConfiguration {
    private final OpenTelemetryLoggingTemplate template = new OpenTelemetryLoggingTemplate();

    @Bean
    @ConditionalOnMissingBean
    SdkLoggerProvider loggerProvider(
            Clock clock,
            LogLimits logLimits,
            Resource resource,
            ObjectProvider<LogRecordProcessor> logRecordProcessors,
            ObjectProvider<OpenTelemetryLoggerProviderBuilderCustomizer> customizers) {
        return template.buildLoggerProvider(clock, logLimits, resource, logRecordProcessors, customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    LogLimits logLimits(OpenTelemetryLoggingProperties properties) {
        return template.logLimits(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(LogRecordExporter.class)
    BatchLogRecordProcessor logRecordProcessor(
            OpenTelemetryLoggingProperties properties,
            ObjectProvider<LogRecordExporter> logRecordExporters,
            ObjectProvider<MeterProvider> meterProvider) {
        return template.batchLogRecordProcessor(properties, logRecordExporters, meterProvider);
    }
}
