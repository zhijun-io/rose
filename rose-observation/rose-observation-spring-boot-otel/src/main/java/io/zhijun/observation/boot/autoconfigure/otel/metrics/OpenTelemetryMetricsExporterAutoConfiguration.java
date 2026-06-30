package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import io.opentelemetry.sdk.metrics.export.MetricExporter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.ConditionalOnOpenTelemetryMetrics;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry metrics.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryMetrics
@Import({ConsoleMetricsExporterConfiguration.class, OtlpMetricsExporterConfiguration.class})
@EnableConfigurationProperties(OpenTelemetryMetricsExporterProperties.class)
public final class OpenTelemetryMetricsExporterAutoConfiguration {

    private final OpenTelemetryMetricsExporterTemplate template = new OpenTelemetryMetricsExporterTemplate();

    @Bean
    OpenTelemetryMeterProviderBuilderCustomizer metricReaderCustomizer(
            OpenTelemetryMetricsExporterProperties properties, ObjectProvider<MetricExporter> metricExporters) {
        return template.metricReaderCustomizer(properties, metricExporters);
    }
}
