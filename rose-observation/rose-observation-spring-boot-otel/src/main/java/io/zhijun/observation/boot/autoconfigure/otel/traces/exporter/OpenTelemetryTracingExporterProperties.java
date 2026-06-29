package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfig;

/**
 * Configuration properties for OpenTelemetry tracing exporters.
 */
@ConfigurationProperties(prefix = OpenTelemetryTracingExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryTracingExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.traces.exporter";

    public static final String TYPE_PROPERTY = CONFIG_PREFIX + ".type";

    public static final String OTLP_CONFIG_PREFIX = CONFIG_PREFIX + ".otlp";

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
