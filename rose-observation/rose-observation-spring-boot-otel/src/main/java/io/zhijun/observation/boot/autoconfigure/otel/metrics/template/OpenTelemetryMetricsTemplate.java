package io.zhijun.observation.boot.autoconfigure.otel.metrics.template;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;

/**
 * Builds metrics SDK components for the OpenTelemetry auto-configuration.
 */
public final class OpenTelemetryMetricsTemplate {

    public static final String INSTRUMENTATION_SCOPE_NAME = "org.springframework.boot";

    public SdkMeterProvider buildMeterProvider(
            Clock clock,
            Resource resource,
            ObjectProvider<OpenTelemetryMeterProviderBuilderCustomizer> customizers) {
        SdkMeterProviderBuilder builder = SdkMeterProvider.builder().setClock(clock).setResource(resource);
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    public CardinalityLimitSelector cardinalityLimitSelector(OpenTelemetryMetricsProperties properties) {
        return instrumentType -> properties.getCardinalityLimit();
    }

    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(INSTRUMENTATION_SCOPE_NAME);
    }
}
