package io.zhijun.observation.boot.autoconfigure.otel.traces;

import io.opentelemetry.context.propagation.TextMapPropagator;

import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties;

/**
 * Builds trace propagation beans.
 */
public final class OpenTelemetryPropagationTemplate {

    public TextMapPropagator create(OpenTelemetryPropagationProperties properties) {
        return CompositeTextMapPropagator.create(properties);
    }

    public TextMapPropagator noop() {
        return TextMapPropagator.noop();
    }
}
