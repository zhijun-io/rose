package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;

/**
 * Configuration properties for Micrometer Metrics Registry OTLP.
 */
@ConfigurationProperties(prefix = MicrometerRegistryOtlpProperties.CONFIG_PREFIX)
public class MicrometerRegistryOtlpProperties {

    public static final String CONFIG_PREFIX = OpenTelemetryExporterProperties.MICROMETER_REGISTRY_CONFIG_PREFIX;

    public static final String ENABLED_PROPERTY = OpenTelemetryExporterProperties.MICROMETER_REGISTRY_ENABLED_PROPERTY;

    /**
     * Whether to enable the Micrometer Metrics Registry OTLP.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
