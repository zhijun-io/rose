package io.zhijun.observation.boot.autoconfigure.otel.common;

/**
 * Type of OpenTelemetry exporter.
 */
public enum ExporterType {
    CONSOLE,

    OTLP,

    NONE
}
