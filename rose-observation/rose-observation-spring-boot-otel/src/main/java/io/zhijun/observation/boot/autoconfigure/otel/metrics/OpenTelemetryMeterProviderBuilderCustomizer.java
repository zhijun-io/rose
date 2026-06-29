package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryBuilderCustomizer;

/**
 * Customizes the {@link SdkMeterProviderBuilder} used to build the autoconfigured {@link SdkMeterProvider}.
 */
@FunctionalInterface
public interface OpenTelemetryMeterProviderBuilderCustomizer
        extends OpenTelemetryBuilderCustomizer<SdkMeterProviderBuilder> {
}
