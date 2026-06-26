package io.zhijun.observation.boot.autoconfigure.otel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OpenTelemetry.
 */
@ConfigurationProperties(prefix = OpenTelemetryProperties.CONFIG_PREFIX)
public class OpenTelemetryProperties {

    public static final String CONFIG_PREFIX = "rose.otel";

    public static final String ENABLED_PROPERTY = CONFIG_PREFIX + ".enabled";

    public static final String COMPATIBILITY_ENV_VAR_SPEC_PROPERTY = CONFIG_PREFIX + ".compatibility.environment-variable-specification";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
