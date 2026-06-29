package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.template;

import java.util.concurrent.Executors;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

/**
 * Builds metrics exporter wiring for OpenTelemetry auto-configuration.
 */
public final class OpenTelemetryMetricsExporterTemplate {

    private static final String THREAD_NAME_PREFIX = "otel-metrics";

    public OpenTelemetryMeterProviderBuilderCustomizer metricReaderCustomizer(
            OpenTelemetryMetricsExporterProperties properties, ObjectProvider<MetricExporter> metricExporters) {
        NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_NAME_PREFIX);
        return builder -> metricExporters.orderedStream().forEach(metricExporter -> builder.registerMetricReader(
                PeriodicMetricReader.builder(metricExporter)
                        .setInterval(properties.getInterval())
                        .setExecutor(Executors.newSingleThreadScheduledExecutor(threadFactory))
                        .build()));
    }
}
