package io.zhijun.boot.actuate.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for monitored task scheduling.
 */
@ConfigurationProperties(prefix = MonitoredSchedulingProperties.CONFIG_PREFIX)
public class MonitoredSchedulingProperties {

    public static final String CONFIG_PREFIX = "rose.scheduling.monitored";

    private boolean enabled = true;

    private int poolSize = 1;

    private String threadNamePrefix = "rose-scheduling-";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }
}
