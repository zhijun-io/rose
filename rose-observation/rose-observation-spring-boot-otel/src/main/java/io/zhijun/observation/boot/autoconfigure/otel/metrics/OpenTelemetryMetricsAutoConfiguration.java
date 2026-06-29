package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.template.OpenTelemetryMetricsTemplate;

/**
 * Auto-configuration for OpenTelemetry metrics.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryMetrics
@EnableConfigurationProperties(OpenTelemetryMetricsProperties.class)
public final class OpenTelemetryMetricsAutoConfiguration {
    private final OpenTelemetryMetricsTemplate template = new OpenTelemetryMetricsTemplate();

    @Bean
    @ConditionalOnMissingBean
    SdkMeterProvider meterProvider(
            Clock clock,
            Resource resource,
            ObjectProvider<OpenTelemetryMeterProviderBuilderCustomizer> customizers) {
        return template.buildMeterProvider(clock, resource, customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    CardinalityLimitSelector cardinalityLimitSelector(OpenTelemetryMetricsProperties properties) {
        return template.cardinalityLimitSelector(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OpenTelemetry.class)
    Meter meter(OpenTelemetry openTelemetry) {
        return template.meter(openTelemetry);
    }
}
