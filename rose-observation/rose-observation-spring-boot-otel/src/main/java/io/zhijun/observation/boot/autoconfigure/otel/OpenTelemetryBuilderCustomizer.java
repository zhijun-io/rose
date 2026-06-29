package io.zhijun.observation.boot.autoconfigure.otel;

/**
 * Customizes an OpenTelemetry builder.
 *
 * @param <T> the builder type
 */
@FunctionalInterface
public interface OpenTelemetryBuilderCustomizer<T> {

    void customize(T builder);
}
