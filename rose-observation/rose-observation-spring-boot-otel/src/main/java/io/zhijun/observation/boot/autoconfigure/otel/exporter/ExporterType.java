package io.zhijun.observation.boot.autoconfigure.otel.exporter;

/**
 * Type of OpenTelemetry exporter.
 */
public enum ExporterType {
    CONSOLE,

    OTLP,

    NONE
}
