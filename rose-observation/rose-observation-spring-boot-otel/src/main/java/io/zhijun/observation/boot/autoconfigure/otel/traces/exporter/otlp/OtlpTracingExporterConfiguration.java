package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterTransportConfigurer;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.ConditionalOnOpenTelemetryTracingExporter;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterProperties;

/**
 * Auto-configuration for exporting traces via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpSpanExporter.class)
@ConditionalOnOpenTelemetryTracingExporter("otlp")
public final class OtlpTracingExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpTracingExporterConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    PropertiesOtlpTracingConnectionDetails otlpTracingConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryTracingExporterProperties properties) {
        return new PropertiesOtlpTracingConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpTracingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryTracingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = Protocol.HTTP_PROTOBUF.configValue(),
            matchIfMissing = true)
    OtlpHttpSpanExporter otlpHttpSpanExporter(
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
        logger.info(
                "Configuring OpenTelemetry HTTP/Protobuf span exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpTracingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryTracingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = Protocol.GRPC.configValue())
    OtlpGrpcSpanExporter otlpGrpcSpanExporter(
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
        logger.info(
                "Configuring OpenTelemetry gRPC span exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.GRPC));
        return builder.build();
    }

    /**
     * Implementation of {@link OtlpTracingConnectionDetails} that uses properties to determine the OTLP endpoint.
     */
    static class PropertiesOtlpTracingConnectionDetails implements OtlpTracingConnectionDetails {

        private final OpenTelemetryExporterProperties commonProperties;
        private final OpenTelemetryTracingExporterProperties properties;

        PropertiesOtlpTracingConnectionDetails(
                OpenTelemetryExporterProperties commonProperties, OpenTelemetryTracingExporterProperties properties) {
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
