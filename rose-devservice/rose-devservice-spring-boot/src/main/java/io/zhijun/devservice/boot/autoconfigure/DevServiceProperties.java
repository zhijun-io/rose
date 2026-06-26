package io.zhijun.devservice.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global dev services properties.
 */
@ConfigurationProperties(prefix = DevServiceProperties.CONFIG_PREFIX)
public class DevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev";

    /**
     * Global switch for Rose Dev Services auto-configuration.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
