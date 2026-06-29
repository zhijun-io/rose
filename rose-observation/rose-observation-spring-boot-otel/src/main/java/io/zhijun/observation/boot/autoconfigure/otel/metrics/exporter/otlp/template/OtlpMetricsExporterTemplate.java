package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.template;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.internal.view.Base2ExponentialHistogramAggregation;
import io.opentelemetry.sdk.metrics.internal.view.ExplicitBucketHistogramAggregation;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterTransportConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.HistogramAggregationStrategy;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

/**
 * Builds OTLP metric exporters for the auto-configuration.
 */
public final class OtlpMetricsExporterTemplate {

    public OtlpHttpMetricExporter buildHttpMetricExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties properties,
            OtlpMetricsConnectionDetails connectionDetails) {
        OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setAggregationTemporalitySelector(aggregationTemporalitySelector(properties))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureHttpMetricTransport(builder, commonProperties, properties.getOtlp());
        return builder.build();
    }

    public OtlpGrpcMetricExporter buildGrpcMetricExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties properties,
            OtlpMetricsConnectionDetails connectionDetails) {
        OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.GRPC))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setAggregationTemporalitySelector(aggregationTemporalitySelector(properties))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureGrpcMetricTransport(builder, commonProperties, properties.getOtlp());
        return builder.build();
    }

    public OpenTelemetryMeterProviderBuilderCustomizer histogramAggregation(
            OpenTelemetryMetricsExporterProperties properties) {
        return builder -> builder.registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder().setAggregation(resolveHistogramAggregation(properties)).build());
    }

    public AggregationTemporalitySelector aggregationTemporalitySelector(
            OpenTelemetryMetricsExporterProperties properties) {
        switch (properties.getAggregationTemporality()) {
            case CUMULATIVE:
                return AggregationTemporalitySelector.alwaysCumulative();
            case DELTA:
                return AggregationTemporalitySelector.deltaPreferred();
            case LOW_MEMORY:
            default:
                return AggregationTemporalitySelector.lowMemory();
        }
    }

    private static io.opentelemetry.sdk.metrics.Aggregation resolveHistogramAggregation(
            OpenTelemetryMetricsExporterProperties properties) {
        if (properties.getHistogramAggregation() == HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM) {
            return Base2ExponentialHistogramAggregation.getDefault();
        }
        return ExplicitBucketHistogramAggregation.getDefault();
    }
}
