package io.zhijun.observability.otel.autoconfigure.traces.exporter;

import org.springframework.lang.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import io.zhijun.observability.otel.autoconfigure.exporter.ExporterType;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpExporterConfig;

/**
 * Configuration properties for OpenTelemetry tracing exporters.
 */
@ConfigurationProperties(prefix = OpenTelemetryTracingExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryTracingExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.traces.exporter";

    /**
     * The type of OpenTelemetry exporter to use.
     */
    @Nullable
    private ExporterType type;

    /**
     * Options for the OTLP exporter.
     */
    @NestedConfigurationProperty
    private final OtlpExporterConfig otlp = new OtlpExporterConfig();

    @Nullable
    public ExporterType getType() {
        return type;
    }

    public void setType(@Nullable ExporterType type) {
        this.type = type;
    }

    public OtlpExporterConfig getOtlp() {
        return otlp;
    }

}
