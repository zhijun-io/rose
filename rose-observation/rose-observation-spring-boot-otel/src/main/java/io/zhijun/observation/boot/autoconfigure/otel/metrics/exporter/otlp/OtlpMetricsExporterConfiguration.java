package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.ConditionalOnOpenTelemetryMetricsExporter;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.HistogramAggregationStrategy;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

/**
 * Auto-configuration for exporting metrics via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpMetricExporter.class)
@ConditionalOnOpenTelemetryMetricsExporter("otlp")
public final class OtlpMetricsExporterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtlpMetricsExporterConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    PropertiesOtlpMetricsConnectionDetails otlpMetricsConnectionDetails(OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties) {
        return new PropertiesOtlpMetricsConnectionDetails(commonProperties, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpMetricsConnectionDetails.class)
    @ConditionalOnProperty(prefix = OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp", name = "protocol", havingValue = "http_protobuf", matchIfMissing = true)
    OtlpHttpMetricExporter otlpHttpMetricExporter(OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties, OtlpMetricsConnectionDetails connectionDetails) {
        OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setAggregationTemporalitySelector(getAggregationTemporalitySelector(properties))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.applyTls(builder, commonProperties, properties.getOtlp(),
                OtlpHttpMetricExporterBuilder::setTrustedCertificates, OtlpHttpMetricExporterBuilder::setClientTls);
        OtlpExporterTransportConfigurer.applyProxy(builder, commonProperties, properties.getOtlp(),
                OtlpHttpMetricExporterBuilder::setProxyOptions);
        logger.info("Configuring OpenTelemetry HTTP/Protobuf metric exporter with endpoint: {}", connectionDetails.getUrl(Protocol.HTTP_PROTOBUF));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OtlpMetricsConnectionDetails.class)
    @ConditionalOnProperty(prefix = OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp", name = "protocol", havingValue = "grpc")
    OtlpGrpcMetricExporter otlpGrpcMetricExporter(OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties, OtlpMetricsConnectionDetails connectionDetails) {
        OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder()
                .setEndpoint(connectionDetails.getUrl(Protocol.GRPC))
                .setTimeout(OtlpExporterConfigurer.timeout(commonProperties, properties.getOtlp()))
                .setConnectTimeout(OtlpExporterConfigurer.connectTimeout(commonProperties, properties.getOtlp()))
                .setCompression(OtlpExporterConfigurer.compression(commonProperties, properties.getOtlp()))
                .setAggregationTemporalitySelector(getAggregationTemporalitySelector(properties))
                .setMemoryMode(OtlpExporterConfigurer.memoryMode(commonProperties));
        builder.setRetryPolicy(OtlpExporterConfigurer.retryPolicy(commonProperties, properties.getOtlp()));
        OtlpExporterConfigurer.applyHeaders(builder::addHeader, commonProperties, properties.getOtlp());
        OtlpExporterTransportConfigurer.applyTls(builder, commonProperties, properties.getOtlp(),
                OtlpGrpcMetricExporterBuilder::setTrustedCertificates, OtlpGrpcMetricExporterBuilder::setClientTls);
        logger.info("Configuring OpenTelemetry gRPC metric exporter with endpoint: {}", connectionDetails.getUrl(Protocol.GRPC));
        return builder.build();
    }

    AggregationTemporalitySelector getAggregationTemporalitySelector(OpenTelemetryMetricsExporterProperties properties) {
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

    @Bean
    OpenTelemetryMeterProviderBuilderCustomizer histogramAggregation(OpenTelemetryMetricsExporterProperties properties) {
        return builder -> builder.registerView(
                InstrumentSelector.builder()
                        .setType(InstrumentType.HISTOGRAM)
                        .build(),
                View.builder()
                        .setAggregation(resolveHistogramAggregation(properties))
                        .build());
    }

    private static io.opentelemetry.sdk.metrics.Aggregation resolveHistogramAggregation(
            OpenTelemetryMetricsExporterProperties properties) {
        if (properties.getHistogramAggregation()
                == HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM) {
            return Base2ExponentialHistogramAggregation.getDefault();
        }
        return ExplicitBucketHistogramAggregation.getDefault();
    }

    /**
     * Implementation of {@link OtlpMetricsConnectionDetails} that uses properties to determine the OTLP endpoint.
     */
    static class PropertiesOtlpMetricsConnectionDetails implements OtlpMetricsConnectionDetails {

        private final OpenTelemetryExporterProperties commonProperties;
        private final OpenTelemetryMetricsExporterProperties properties;

        PropertiesOtlpMetricsConnectionDetails(OpenTelemetryExporterProperties commonProperties,
                OpenTelemetryMetricsExporterProperties properties) {
            this.commonProperties = commonProperties;
            this.properties = properties;
        }

        @Override
        public String getUrl(Protocol protocol) {
            return OtlpConnectionUrls.resolve(protocol, commonProperties, properties.getOtlp(), METRICS_PATH,
                    DEFAULT_HTTP_PROTOBUF_ENDPOINT, DEFAULT_GRPC_ENDPOINT);
        }

    }

}
