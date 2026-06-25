package io.zhijun.mybatisplus.boot.autoconfigure.observation;

import java.time.Duration;

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
    private boolean enabled = false;

    /**
     * Threshold beyond which a SQL execution is logged at WARN level.
     * {@code null} disables slow-query detection.
     */
    private Duration slowQueryThreshold;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    public void setSlowQueryThreshold(Duration slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }

    /**
     * Returns the threshold in millis, or {@code 0} if not configured.
     */
    public long getSlowQueryThresholdMillis() {
        return slowQueryThreshold == null ? 0 : slowQueryThreshold.toMillis();
    }

}
