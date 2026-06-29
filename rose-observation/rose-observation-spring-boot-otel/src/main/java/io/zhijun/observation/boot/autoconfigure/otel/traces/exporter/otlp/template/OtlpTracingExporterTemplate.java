package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.template;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterTransportConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.OtlpTracingConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterProperties;

/**
 * Builds OTLP tracing exporters for the auto-configuration.
 */
public final class OtlpTracingExporterTemplate {

    public OtlpHttpSpanExporter buildHttpSpanExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryTracingExporterProperties properties,
            OtlpTracingConnectionDetails connectionDetails,
            ObjectProvider<MeterProvider> meterProvider) {
        OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureHttpTraceTransport(builder, commonProperties, properties.getOtlp());
        OtlpExporterConfigurer.configureExporterMetrics(
                meterProvider, commonProperties, properties.getOtlp(), builder::setMeterProvider);
        return builder.build();
    }

    public OtlpGrpcSpanExporter buildGrpcSpanExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryTracingExporterProperties properties,
            OtlpTracingConnectionDetails connectionDetails,
            ObjectProvider<MeterProvider> meterProvider) {
        OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.GRPC))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.configureGrpcTraceTransport(builder, commonProperties, properties.getOtlp());
        OtlpExporterConfigurer.configureExporterMetrics(
                meterProvider, commonProperties, properties.getOtlp(), builder::setMeterProvider);
        return builder.build();
    }

    /**
     * Implementation of {@link OtlpTracingConnectionDetails} that uses properties to determine the OTLP endpoint.
     */
    public static final class PropertiesOtlpTracingConnectionDetails implements OtlpTracingConnectionDetails {

        private final OpenTelemetryExporterProperties commonProperties;
        private final OpenTelemetryTracingExporterProperties properties;

        public PropertiesOtlpTracingConnectionDetails(
                OpenTelemetryExporterProperties commonProperties,
                OpenTelemetryTracingExporterProperties properties) {
            this.commonProperties = commonProperties;
            this.properties = properties;
        }

        @Override
        public String getUrl(Protocol protocol) {
            return OtlpConnectionUrls.resolve(
                    protocol,
                    commonProperties,
                    properties.getOtlp(),
                    TRACES_PATH,
                    DEFAULT_HTTP_PROTOBUF_ENDPOINT,
                    DEFAULT_GRPC_ENDPOINT);
        }
    }
}
