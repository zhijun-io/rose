package io.zhijun.observability.otel.autoconfigure.metrics.exporter;

import java.util.concurrent.Executors;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.zhijun.observability.otel.autoconfigure.metrics.ConditionalOnOpenTelemetryMetrics;
import io.zhijun.observability.otel.autoconfigure.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.console.ConsoleMetricsExporterConfiguration;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.otlp.OtlpMetricsExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry metrics.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryMetrics
@Import({ ConsoleMetricsExporterConfiguration.class, OtlpMetricsExporterConfiguration.class })
@EnableConfigurationProperties(OpenTelemetryMetricsExporterProperties.class)
public final class OpenTelemetryMetricsExporterAutoConfiguration {

    private static final String THREAD_NAME_PREFIX = "otel-metrics";

    @Bean
    OpenTelemetryMeterProviderBuilderCustomizer metricReaderCustomizer(
            OpenTelemetryMetricsExporterProperties properties,
            ObjectProvider<MetricExporter> metricExporters) {
        NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_NAME_PREFIX);
        return builder -> {
            for (MetricExporter metricExporter : metricExporters.orderedStream()
                    .collect(java.util.stream.Collectors.toList())) {
                builder.registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                        .setInterval(properties.getInterval())
                        .setExecutor(Executors.newSingleThreadScheduledExecutor(threadFactory))
                        .build());
            }
        };
    }
}
