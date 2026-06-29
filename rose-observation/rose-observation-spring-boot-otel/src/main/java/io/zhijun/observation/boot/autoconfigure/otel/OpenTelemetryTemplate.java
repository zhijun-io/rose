package io.zhijun.observation.boot.autoconfigure.otel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import org.springframework.beans.factory.ObjectProvider;

/**
 * Builds the top-level OpenTelemetry SDK and related fallback beans.
 */
public final class OpenTelemetryTemplate {

    public OpenTelemetrySdk buildOpenTelemetrySdk(
            ObjectProvider<SdkLoggerProvider> loggerProvider,
            ObjectProvider<SdkMeterProvider> meterProvider,
            ObjectProvider<SdkTracerProvider> tracerProvider,
            ObjectProvider<ContextPropagators> propagators) {
        OpenTelemetrySdkBuilder openTelemetryBuilder = OpenTelemetrySdk.builder();
        loggerProvider.ifAvailable(openTelemetryBuilder::setLoggerProvider);
        meterProvider.ifAvailable(openTelemetryBuilder::setMeterProvider);
        tracerProvider.ifAvailable(openTelemetryBuilder::setTracerProvider);
        propagators.ifAvailable(openTelemetryBuilder::setPropagators);
        return openTelemetryBuilder.build();
    }

    public OpenTelemetry noopOpenTelemetry() {
        return OpenTelemetry.noop();
    }

    public Clock clock() {
        return Clock.getDefault();
    }

    public Resource resource() {
        return Resource.empty();
    }
}
