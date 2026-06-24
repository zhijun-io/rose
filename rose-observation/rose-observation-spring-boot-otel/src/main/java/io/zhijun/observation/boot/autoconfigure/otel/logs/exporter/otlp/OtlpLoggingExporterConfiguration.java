package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;

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
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.ConditionalOnOpenTelemetryLoggingExporter;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.OpenTelemetryLoggingExporterProperties;

/**
 * Configuration for exporting logs via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpLogRecordExporter.class)
@ConditionalOnOpenTelemetryLoggingExporter("otlp")
public final class OtlpLoggingExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpLoggingExporterConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    OtlpLoggingConnectionDetails otlpLoggingConnectionDetails(OpenTelemetryExporterProperties commonProperties, OpenTelemetryLoggingExporterProperties properties) {
        return new PropertiesOtlpLoggingConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpLoggingConnectionDetails.class)
    @ConditionalOnProperty(prefix = OpenTelemetryLoggingExporterProperties.CONFIG_PREFIX + ".otlp", name = "protocol", havingValue = "http_protobuf", matchIfMissing = true)
    OtlpHttpLogRecordExporter otlpHttpLogRecordExporter(OpenTelemetryExporterProperties commonProperties, OpenTelemetryLoggingExporterProperties properties, OtlpLoggingConnectionDetails connectionDetails, ObjectProvider<MeterProvider> meterProvider) {
        OtlpHttpLogRecordExporterBuilder builder = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.applyTls(builder, commonProperties, properties.getOtlp(),
                OtlpHttpLogRecordExporterBuilder::setTrustedCertificates, OtlpHttpLogRecordExporterBuilder::setClientTls);
        OtlpExporterTransportConfigurer.applyProxy(builder, commonProperties, properties.getOtlp(),
                OtlpHttpLogRecordExporterBuilder::setProxyOptions);
        OtlpExporterConfigurer.configureExporterMetrics(meterProvider, commonProperties, properties.getOtlp(),
                builder::setMeterProvider);
        logger.info("Configuring OpenTelemetry HTTP/Protobuf log exporter with endpoint: {}", connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpLoggingConnectionDetails.class)
    @ConditionalOnProperty(prefix = OpenTelemetryLoggingExporterProperties.CONFIG_PREFIX + ".otlp", name = "protocol", havingValue = "grpc")
    OtlpGrpcLogRecordExporter otlpGrpcLogRecordExporter(OpenTelemetryExporterProperties commonProperties, OpenTelemetryLoggingExporterProperties properties, OtlpLoggingConnectionDetails connectionDetails, ObjectProvider<MeterProvider> meterProvider) {
        OtlpGrpcLogRecordExporterBuilder builder = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.GRPC))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.applyTls(builder, commonProperties, properties.getOtlp(),
                OtlpGrpcLogRecordExporterBuilder::setTrustedCertificates, OtlpGrpcLogRecordExporterBuilder::setClientTls);
        OtlpExporterConfigurer.configureExporterMetrics(meterProvider, commonProperties, properties.getOtlp(),
                builder::setMeterProvider);
        logger.info("Configuring OpenTelemetry gRPC log exporter with endpoint: {}", connectionDetails.getUrl(Protocol.GRPC));
        return builder.build();
    }

    /**
     * Implementation of {@link OtlpLoggingConnectionDetails} that uses properties to determine the OTLP endpoint.
     */
    static class PropertiesOtlpLoggingConnectionDetails implements OtlpLoggingConnectionDetails {

        private final OpenTelemetryExporterProperties commonProperties;
        private final OpenTelemetryLoggingExporterProperties properties;

        PropertiesOtlpLoggingConnectionDetails(OpenTelemetryExporterProperties commonProperties,
                OpenTelemetryLoggingExporterProperties properties) {
            this.commonProperties = commonProperties;
            this.properties = properties;
        }

        @Override
        public String getUrl(Protocol protocol) {
            return OtlpConnectionUrls.resolve(protocol, commonProperties, properties.getOtlp(), LOGS_PATH,
                    DEFAULT_HTTP_PROTOBUF_ENDPOINT, DEFAULT_GRPC_ENDPOINT);
        }

    }

}
