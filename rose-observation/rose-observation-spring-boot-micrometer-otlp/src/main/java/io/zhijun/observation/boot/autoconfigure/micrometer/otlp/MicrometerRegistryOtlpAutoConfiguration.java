package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.ConditionalOnOpenTelemetryMetricsExporter;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.common.ExporterTypeNames;
import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsConnectionDetails;

/**
 * Auto-configuration for Micrometer Registry OTLP export.
 */
@AutoConfiguration(
        after = {
            MetricsAutoConfiguration.class,
            OpenTelemetryMetricsAutoConfiguration.class,
            OpenTelemetryMetricsExporterAutoConfiguration.class
        },
        before = {CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@Conditional(MicrometerRegistryOtlpAutoConfiguration.MicrometerBridgeDisabled.class)
@ConditionalOnProperty(
        prefix = MicrometerRegistryOtlpProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@ConditionalOnOpenTelemetryMetricsExporter(ExporterTypeNames.OTLP)
@EnableConfigurationProperties(MicrometerRegistryOtlpProperties.class)
public final class MicrometerRegistryOtlpAutoConfiguration {
    private final MicrometerRegistryOtlpTemplate template = new MicrometerRegistryOtlpTemplate();

    static class MicrometerBridgeDisabled extends AnyNestedCondition {

        MicrometerBridgeDisabled() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(
                prefix = OpenTelemetryMetricsProperties.MICROMETER_BRIDGE_CONFIG_PREFIX,
                name = "enabled",
                havingValue = "false",
                matchIfMissing = true)
        static class Disabled {}
    }

    @Bean
    @ConditionalOnMissingBean(OtlpConfig.class)
    MicrometerOtlpConfig otlpConfig(
            OtlpMetricsConnectionDetails connectionDetails,
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties metricsProperties,
            Resource resource) {
        return template.build(connectionDetails, commonProperties, metricsProperties, resource);
    }

    @Bean
    OtlpMeterRegistry otlpMeterRegistry(Clock clock, OtlpConfig otlpConfig) {
        return new OtlpMeterRegistry(otlpConfig, clock);
    }
}
