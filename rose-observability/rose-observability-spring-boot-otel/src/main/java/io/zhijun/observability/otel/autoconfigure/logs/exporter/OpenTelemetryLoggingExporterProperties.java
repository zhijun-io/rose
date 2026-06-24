package io.zhijun.observability.otel.autoconfigure.logs.exporter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;

import io.zhijun.observability.otel.autoconfigure.exporter.ExporterType;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpExporterConfig;

/**
 * Configuration properties for exporting OpenTelemetry logs.
 */
@ConfigurationProperties(prefix = OpenTelemetryLoggingExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryLoggingExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.logs.exporter";

    @Nullable
    private ExporterType type;

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
