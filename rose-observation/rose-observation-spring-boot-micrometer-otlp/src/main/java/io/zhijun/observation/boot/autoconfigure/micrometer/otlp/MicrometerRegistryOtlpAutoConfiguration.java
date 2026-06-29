package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterTypeNames;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.ConditionalOnOpenTelemetryMetricsExporter;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceAutoConfiguration;

/**
 * Auto-configuration for Micrometer Registry OTLP export.
 */
@AutoConfiguration(
        after = {
            MetricsAutoConfiguration.class,
            OpenTelemetryMetricsAutoConfiguration.class,
            OpenTelemetryMetricsExporterAutoConfiguration.class,
            OpenTelemetryResourceAutoConfiguration.class
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

    private static final Set<String> RESERVED_RESOURCE_ATTRIBUTES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("telemetry.sdk.language", "telemetry.sdk.name", "telemetry.sdk.version")));

    @Bean
    @ConditionalOnMissingBean(OtlpConfig.class)
    MicrometerOtlpConfig otlpConfig(
            OtlpMetricsConnectionDetails connectionDetails,
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties metricsProperties,
            Resource resource) {
        Protocol protocol = metricsProperties.getOtlp().getProtocol() != null
                ? metricsProperties.getOtlp().getProtocol()
                : commonProperties.getOtlp().getProtocol();
        return MicrometerOtlpConfig.builder()
                .url(connectionDetails.getUrl(protocol))
                .step(metricsProperties.getInterval())
                .addResourceAttributes(resource.getAttributes().asMap().entrySet().stream()
                        .filter(entry -> !RESERVED_RESOURCE_ATTRIBUTES.contains(
                                entry.getKey().getKey()))
                        .collect(
                                HashMap::new,
                                (m, e) ->
                                        m.put(e.getKey().getKey(), e.getValue().toString()),
                                HashMap::putAll))
                .build();
    }

    @Bean
    OtlpMeterRegistry otlpMeterRegistry(Clock clock, OtlpConfig otlpConfig) {
        return new OtlpMeterRegistry(otlpConfig, clock);
    }
}
