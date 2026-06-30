package io.zhijun.observation.boot.autoconfigure.otel.logs;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.common.ProtocolNames;
import io.zhijun.observation.boot.autoconfigure.otel.logs.ConditionalOnOpenTelemetryLoggingExporter;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggingExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.logs.PropertiesOtlpLoggingConnectionDetails;

/**
 * Configuration for exporting logs via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpLogRecordExporter.class)
@ConditionalOnOpenTelemetryLoggingExporter("otlp")
public final class OtlpLoggingExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpLoggingExporterConfiguration.class);
    private final OtlpLoggingExporterTemplate template = new OtlpLoggingExporterTemplate();

    @Bean
    @ConditionalOnMissingBean
    OtlpLoggingConnectionDetails otlpLoggingConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryLoggingExporterProperties properties) {
        return new PropertiesOtlpLoggingConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpLoggingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryLoggingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.HTTP_PROTOBUF,
            matchIfMissing = true)
    OtlpHttpLogRecordExporter otlpHttpLogRecordExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryLoggingExporterProperties properties,
            OtlpLoggingConnectionDetails connectionDetails,
            org.springframework.beans.factory.ObjectProvider<io.opentelemetry.api.metrics.MeterProvider> meterProvider) {
        OtlpHttpLogRecordExporter exporter =
                template.buildHttpLogExporter(commonProperties, properties, connectionDetails, meterProvider);
        logger.info(
                "Configuring OpenTelemetry HTTP/Protobuf log exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return exporter;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpLoggingConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryLoggingExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.GRPC)
    OtlpGrpcLogRecordExporter otlpGrpcLogRecordExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryLoggingExporterProperties properties,
            OtlpLoggingConnectionDetails connectionDetails,
            org.springframework.beans.factory.ObjectProvider<io.opentelemetry.api.metrics.MeterProvider> meterProvider) {
        OtlpGrpcLogRecordExporter exporter =
                template.buildGrpcLogExporter(commonProperties, properties, connectionDetails, meterProvider);
        logger.info(
                "Configuring OpenTelemetry gRPC log exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.GRPC));
        return exporter;
    }
}
