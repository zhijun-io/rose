package io.zhijun.mybatisplus.core.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SQL observation.
 */
@ConfigurationProperties(prefix = SqlObservationProperties.CONFIG_PREFIX)
public class SqlObservationProperties {

    public static final String CONFIG_PREFIX = "rose.mybatis-plus.observation";

    /**
     * Whether SQL observation is enabled.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
