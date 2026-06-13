package io.zhijun.opentelemetry.micrometer.registry.otlp.autoconfigure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.HistogramFlavor;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import io.micrometer.registry.otlp.OtlpMetricsSender;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.zhijun.opentelemetry.autoconfigure.exporter.OpenTelemetryExporterProperties;
import io.zhijun.opentelemetry.autoconfigure.exporter.otlp.Protocol;
import io.zhijun.opentelemetry.autoconfigure.metrics.OpenTelemetryMetricsAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.ConditionalOnOpenTelemetryMetricsExporter;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.HistogramAggregationStrategy;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.opentelemetry.autoconfigure.resource.OpenTelemetryResourceAutoConfiguration;

/**
 * Auto-configuration for Micrometer Registry OTLP export.
 */
@AutoConfiguration(
        after = { MetricsAutoConfiguration.class, OpenTelemetryMetricsAutoConfiguration.class,
                OpenTelemetryMetricsExporterAutoConfiguration.class, OpenTelemetryResourceAutoConfiguration.class },
        before = { CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class }
)
@Conditional(MicrometerRegistryOtlpAutoConfiguration.MicrometerBridgeDisabled.class)
@ConditionalOnProperty(prefix = MicrometerRegistryOtlpProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnOpenTelemetryMetricsExporter("otlp")
@EnableConfigurationProperties(MicrometerRegistryOtlpProperties.class)
public final class MicrometerRegistryOtlpAutoConfiguration {

    static class MicrometerBridgeDisabled extends AnyNestedCondition {

        MicrometerBridgeDisabled() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "rose.otel.metrics.micrometer-bridge", name = "enabled", havingValue = "false", matchIfMissing = true)
        static class Disabled {}
    }

    private static final Set<String> RESERVED_RESOURCE_ATTRIBUTES = new HashSet<>(
            Arrays.asList("telemetry.sdk.language", "telemetry.sdk.name", "telemetry.sdk.version"));

    @Bean
    @ConditionalOnMissingBean(OtlpConfig.class)
    MicrometerOtlpConfig otlpConfig(OtlpMetricsConnectionDetails connectionDetails,
                                    OpenTelemetryExporterProperties commonProperties,
                                    OpenTelemetryMetricsExporterProperties metricsProperties,
                                    MicrometerRegistryOtlpProperties registryProperties,
                                    Resource resource) {
        Protocol protocol = metricsProperties.getOtlp().getProtocol() != null
                ? metricsProperties.getOtlp().getProtocol()
                : commonProperties.getOtlp().getProtocol();
        AggregationTemporality temporality;
        switch (metricsProperties.getAggregationTemporality()) {
            case DELTA:
                temporality = AggregationTemporality.DELTA;
                break;
            case CUMULATIVE:
            case LOW_MEMORY:
            default:
                temporality = AggregationTemporality.CUMULATIVE;
                break;
        }
        HistogramAggregationStrategy aggregation = metricsProperties.getHistogramAggregation();
        HistogramFlavor histogramFlavor;
        if (aggregation == HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM) {
            histogramFlavor = HistogramFlavor.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM;
        } else {
            histogramFlavor = HistogramFlavor.EXPLICIT_BUCKET_HISTOGRAM;
        }
        return MicrometerOtlpConfig.builder()
                .url(connectionDetails.getUrl(protocol))
                .step(metricsProperties.getInterval())
                .aggregationTemporality(temporality)
                .histogramFlavor(histogramFlavor)
                .addHeaders(commonProperties.getOtlp().getHeaders())
                .addHeaders(metricsProperties.getOtlp().getHeaders())
                .addResourceAttributes(resource.getAttributes().asMap().entrySet().stream()
                        .filter(entry -> !RESERVED_RESOURCE_ATTRIBUTES.contains(entry.getKey().getKey()))
                        .collect(HashMap::new, (m, e) -> m.put(e.getKey().getKey(), e.getValue().toString()),
                                HashMap::putAll))
                .maxScale(registryProperties.getMaxScale())
                .maxBucketCount(registryProperties.getMaxBucketCount())
                .baseTimeUnit(registryProperties.getBaseTimeUnit())
                .build();
    }

    @Bean
    OtlpMeterRegistry otlpMeterRegistry(Clock clock, OtlpConfig otlpConfig,
                                        ObjectProvider<OtlpMetricsSender> otlpMetricsSender) {
        OtlpMeterRegistry.Builder builder = OtlpMeterRegistry.builder(otlpConfig).clock(clock);
        otlpMetricsSender.ifAvailable(builder::metricsSender);
        return builder.build();
    }
}
