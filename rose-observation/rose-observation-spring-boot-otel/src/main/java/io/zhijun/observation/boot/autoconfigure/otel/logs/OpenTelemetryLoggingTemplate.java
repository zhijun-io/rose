package io.zhijun.observation.boot.autoconfigure.otel.logs;

import java.util.stream.Collectors;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggerProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggingProperties;

/**
 * Builds logging SDK components for the OpenTelemetry auto-configuration.
 */
public final class OpenTelemetryLoggingTemplate {

    public SdkLoggerProvider buildLoggerProvider(
            Clock clock,
            LogLimits logLimits,
            Resource resource,
            ObjectProvider<LogRecordProcessor> logRecordProcessors,
            ObjectProvider<OpenTelemetryLoggerProviderBuilderCustomizer> customizers) {
        SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder()
                .setClock(clock)
                .setLogLimits(() -> logLimits)
                .setResource(resource);
        logRecordProcessors.orderedStream().forEach(loggerProviderBuilder::addLogRecordProcessor);
        customizers.orderedStream().forEach(customizer -> customizer.customize(loggerProviderBuilder));
        return loggerProviderBuilder.build();
    }

    public LogLimits logLimits(OpenTelemetryLoggingProperties properties) {
        return LogLimits.builder()
                .setMaxAttributeValueLength(properties.getLimits().getMaxAttributeValueLength())
                .setMaxNumberOfAttributes(properties.getLimits().getMaxNumberOfAttributes())
                .build();
    }

    public BatchLogRecordProcessor batchLogRecordProcessor(
            OpenTelemetryLoggingProperties properties,
            ObjectProvider<LogRecordExporter> logRecordExporters,
            ObjectProvider<MeterProvider> meterProvider) {
        BatchLogRecordProcessorBuilder builder = BatchLogRecordProcessor.builder(
                        LogRecordExporter.composite(
                                logRecordExporters.orderedStream().collect(Collectors.toList())))
                .setExporterTimeout(properties.getProcessor().getExportTimeout())
                .setScheduleDelay(properties.getProcessor().getScheduleDelay())
                .setMaxExportBatchSize(properties.getProcessor().getMaxExportBatchSize())
                .setMaxQueueSize(properties.getProcessor().getMaxQueueSize());
        if (properties.getProcessor().isMetrics()) {
            meterProvider.ifAvailable(builder::setMeterProvider);
        }
        return builder.build();
    }
}
