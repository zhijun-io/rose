package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.ConditionalOnOpenTelemetryMetrics;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;

/**
 * Auto-configuration for Micrometer metrics bridge to OpenTelemetry.
 */
@AutoConfiguration(
        after = {MetricsAutoConfiguration.class, OpenTelemetryMetricsAutoConfiguration.class},
        before = {CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnProperty(
        prefix = MicrometerMetricsOpenTelemetryBridgeProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@ConditionalOnOpenTelemetryMetrics
@Conditional(OnMicrometerMetricsBridgeEnabledCondition.class)
@EnableConfigurationProperties(MicrometerMetricsOpenTelemetryBridgeProperties.class)
public final class MicrometerMetricsOpenTelemetryBridgeAutoConfiguration {
    private final MicrometerMetricsOpenTelemetryBridgeTemplate template =
            new MicrometerMetricsOpenTelemetryBridgeTemplate();

    // A MeterRegistry used exclusively for reading metrics, e.g. from the Actuator /metrics endpoint.
    // This is necessary because the OpenTelemetryMeterRegistry doesn't annotation reading metrics, but
    // only bridging them to OpenTelemetry. We register this first so that it is the default
    // MeterRegistry used by the Actuator.
    @Bean
    @ConditionalOnBean({Clock.class, OpenTelemetry.class, OpenTelemetryMetricsExporterProperties.class})
    SimpleMeterRegistry simpleMeterRegistry(Clock clock, OpenTelemetryMetricsExporterProperties properties) {
        return template.buildSimpleMeterRegistry(clock, properties);
    }

    @Bean
    @ConditionalOnBean({Clock.class, OpenTelemetry.class})
    MeterRegistry meterRegistry(
            MicrometerMetricsOpenTelemetryBridgeProperties properties, Clock clock, OpenTelemetry openTelemetry) {
        return template.buildMeterRegistry(properties, clock, openTelemetry);
    }
}
