package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.ProtocolNames;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.ConditionalOnOpenTelemetryTracingExporter;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.internal.PropertiesOtlpTracingConnectionDetails;

/**
 * Auto-configuration for exporting traces via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpSpanExporter.class)
@ConditionalOnOpenTelemetryTracingExporter("otlp")
public final class OtlpTracingExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpTracingExporterConfiguration.class);
    private final OtlpTracingExporterTemplate template = new OtlpTracingExporterTemplate();

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails otlpTracingConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryTracingExporterProperties properties) {
        return new PropertiesOtlpTracingConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpTracingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryTracingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.HTTP_PROTOBUF,
            matchIfMissing = true)
    OtlpHttpSpanExporter otlpHttpSpanExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryTracingExporterProperties properties,
            OtlpTracingConnectionDetails connectionDetails,
            org.springframework.beans.factory.ObjectProvider<io.opentelemetry.api.metrics.MeterProvider> meterProvider) {
        OtlpHttpSpanExporter exporter =
                template.buildHttpSpanExporter(commonProperties, properties, connectionDetails, meterProvider);
        logger.info(
                "Configuring OpenTelemetry HTTP/Protobuf span exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return exporter;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpTracingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryTracingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.GRPC)
    OtlpGrpcSpanExporter otlpGrpcSpanExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryTracingExporterProperties properties,
            OtlpTracingConnectionDetails connectionDetails,
            org.springframework.beans.factory.ObjectProvider<io.opentelemetry.api.metrics.MeterProvider> meterProvider) {
        OtlpGrpcSpanExporter exporter =
                template.buildGrpcSpanExporter(commonProperties, properties, connectionDetails, meterProvider);
        logger.info(
                "Configuring OpenTelemetry gRPC span exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.GRPC));
        return exporter;
    }
}
