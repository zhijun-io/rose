package io.zhijun.observability.core;

/**
 * Pluggable telemetry conventions backend (e.g. OpenTelemetry semconv, OpenInference).
 * Modules register beans of this type; {@code rose-observability-spring-boot} selects one at startup.
 */
public interface TelemetryConventionsBackend {

    /**
     * Stable backend identifier (e.g. {@code "opentelemetry"}, {@code "openinference"}).
     */
    String id();

    /**
     * Whether this backend may be selected when {@code rose.observability.conventions.backend} is unset.
     */
    default boolean defaultCandidate() {
        return false;
    }

    static TelemetryConventionsBackend of(String id) {
        return of(id, false);
    }

    static TelemetryConventionsBackend of(String id, boolean defaultCandidate) {
        return new TelemetryConventionsBackend() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public boolean defaultCandidate() {
                return defaultCandidate;
            }
        };
    }
}
