package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter;

import io.zhijun.core.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfig;

/**
 * Configuration properties for exporting OpenTelemetry logs.
 */
@ConfigurationProperties(prefix = OpenTelemetryLoggingExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryLoggingExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.logs.exporter";

    public static final String TYPE_PROPERTY = CONFIG_PREFIX + ".type";

    public static final String OTLP_CONFIG_PREFIX = CONFIG_PREFIX + ".otlp";

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
