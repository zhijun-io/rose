package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterTransportConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.OpenTelemetryLoggingExporterProperties;

/**
 * Builds OTLP logging exporters for the auto-configuration.
 */
public final class OtlpLoggingExporterTemplate {

    public OtlpHttpLogRecordExporter buildHttpLogExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryLoggingExporterProperties properties,
            OtlpLoggingConnectionDetails connectionDetails,
            ObjectProvider<MeterProvider> meterProvider) {
        OtlpHttpLogRecordExporterBuilder builder = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureHttpLogTransport(builder, commonProperties, properties.getOtlp());
        OtlpExporterConfigurer.configureExporterMetrics(
                meterProvider, commonProperties, properties.getOtlp(), builder::setMeterProvider);
        return builder.build();
    }

    public OtlpGrpcLogRecordExporter buildGrpcLogExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryLoggingExporterProperties properties,
            OtlpLoggingConnectionDetails connectionDetails,
            ObjectProvider<MeterProvider> meterProvider) {
        OtlpGrpcLogRecordExporterBuilder builder = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.GRPC))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureGrpcLogTransport(builder, commonProperties, properties.getOtlp());
        OtlpExporterConfigurer.configureExporterMetrics(
                meterProvider, commonProperties, properties.getOtlp(), builder::setMeterProvider);
        return builder.build();
    }
}
