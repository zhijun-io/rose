package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for OpenTelemetry metrics.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryMetrics
@EnableConfigurationProperties(OpenTelemetryMetricsProperties.class)
public final class OpenTelemetryMetricsAutoConfiguration {

    public static final String INSTRUMENTATION_SCOPE_NAME = "org.springframework.boot";

    @Bean
    @ConditionalOnMissingBean
    SdkMeterProvider meterProvider(Clock clock,
                                   OpenTelemetryMetricsProperties properties,
                                   Resource resource,
                                   ObjectProvider<OpenTelemetryMeterProviderBuilderCustomizer> customizers) {
        SdkMeterProviderBuilder builder = SdkMeterProvider.builder()
                .setClock(clock)
                .setResource(resource);
        for (OpenTelemetryMeterProviderBuilderCustomizer customizer : customizers.orderedStream()
                .collect(java.util.stream.Collectors.toList())) {
            customizer.customize(builder);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    CardinalityLimitSelector cardinalityLimitSelector(OpenTelemetryMetricsProperties properties) {
        return instrumentType -> properties.getCardinalityLimit();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OpenTelemetry.class)
    Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(INSTRUMENTATION_SCOPE_NAME);
    }
}
