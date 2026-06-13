package io.zhijun.dev.services.core.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global dev services properties.
 */
@ConfigurationProperties(prefix = DevServicesProperties.CONFIG_PREFIX)
public class DevServicesProperties {

    public static final String CONFIG_PREFIX = "rose.dev.services";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
