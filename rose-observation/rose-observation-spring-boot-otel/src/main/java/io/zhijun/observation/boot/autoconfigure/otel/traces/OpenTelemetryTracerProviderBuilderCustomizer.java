package io.zhijun.observation.boot.autoconfigure.otel.traces;

import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryBuilderCustomizer;

/**
 * Customizes the {@link SdkTracerProviderBuilder} used to build the autoconfigured {@link SdkTracerProvider}.
 */
@FunctionalInterface
public interface OpenTelemetryTracerProviderBuilderCustomizer
        extends OpenTelemetryBuilderCustomizer<SdkTracerProviderBuilder> {
}
