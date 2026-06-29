package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterTypeNames;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.ProtocolNames;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.ConditionalOnOpenTelemetryMetricsExporter;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.template.OtlpMetricsExporterTemplate;

/**
 * Auto-configuration for exporting metrics via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpMetricExporter.class)
@ConditionalOnOpenTelemetryMetricsExporter(ExporterTypeNames.OTLP)
public final class OtlpMetricsExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpMetricsExporterConfiguration.class);
    private final OtlpMetricsExporterTemplate template = new OtlpMetricsExporterTemplate();

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    PropertiesOtlpMetricsConnectionDetails otlpMetricsConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties) {
        return new PropertiesOtlpMetricsConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpMetricsConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryMetricsExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.HTTP_PROTOBUF,
            matchIfMissing = true)
    OtlpHttpMetricExporter otlpHttpMetricExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties properties,
            OtlpMetricsConnectionDetails connectionDetails) {
        OtlpHttpMetricExporter exporter = template.buildHttpMetricExporter(commonProperties, properties, connectionDetails);
        logger.info(
                "Configuring OpenTelemetry HTTP/Protobuf metric exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return exporter;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpMetricsConnectionDetails.class)
    @ConditionalOnProperty(
            prefix = OpenTelemetryMetricsExporterProperties.OTLP_CONFIG_PREFIX,
            name = "protocol",
            havingValue = ProtocolNames.GRPC)
    OtlpGrpcMetricExporter otlpGrpcMetricExporter(
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties properties,
            OtlpMetricsConnectionDetails connectionDetails) {
        OtlpGrpcMetricExporter exporter = template.buildGrpcMetricExporter(commonProperties, properties, connectionDetails);
        logger.info(
                "Configuring OpenTelemetry gRPC metric exporter with endpoint: {}",
                connectionDetails.getUrl(Protocol.GRPC));
        return exporter;
    }

    @Bean
    OpenTelemetryMeterProviderBuilderCustomizer histogramAggregation(OpenTelemetryMetricsExporterProperties properties) {
        return template.histogramAggregation(properties);
    }

    /**
     * Implementation of {@link OtlpMetricsConnectionDetails} that uses properties to determine the OTLP endpoint.
     */
    static class PropertiesOtlpMetricsConnectionDetails implements OtlpMetricsConnectionDetails {

        private final OpenTelemetryExporterProperties commonProperties;
        private final OpenTelemetryMetricsExporterProperties properties;

        PropertiesOtlpMetricsConnectionDetails(
                OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties) {
            this.commonProperties = commonProperties;
            this.properties = properties;
        }

        @Override
        public String getUrl(Protocol protocol) {
            return OtlpConnectionUrls.resolve(
                    protocol,
                    commonProperties,
                    properties.getOtlp(),
                    METRICS_PATH,
                    DEFAULT_HTTP_PROTOBUF_ENDPOINT,
                    DEFAULT_GRPC_ENDPOINT);
        }
    }
}
