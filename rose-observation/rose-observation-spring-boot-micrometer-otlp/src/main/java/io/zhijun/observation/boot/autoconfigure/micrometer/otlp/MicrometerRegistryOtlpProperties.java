package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Micrometer Metrics Registry OTLP.
 */
@ConfigurationProperties(prefix = MicrometerRegistryOtlpProperties.CONFIG_PREFIX)
public class MicrometerRegistryOtlpProperties {

    public static final String CONFIG_PREFIX = "rose.otel.exporter.otlp.micrometer";

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
