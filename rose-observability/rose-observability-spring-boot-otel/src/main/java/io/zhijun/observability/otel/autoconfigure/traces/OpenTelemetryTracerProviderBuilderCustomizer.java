package io.zhijun.observability.otel.autoconfigure.traces;

import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

/**
 * Customizes the {@link SdkTracerProviderBuilder} used to build the autoconfigured {@link SdkTracerProvider}.
 */
@FunctionalInterface
public interface OpenTelemetryTracerProviderBuilderCustomizer {

	void customize(SdkTracerProviderBuilder builder);

}
