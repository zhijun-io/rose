package io.zhijun.multitenancy.autoconfigure.async;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for multitenancy async propagation.
 */
@ConfigurationProperties(prefix = MultitenancyAsyncProperties.CONFIG_PREFIX)
public class MultitenancyAsyncProperties {

    public static final String CONFIG_PREFIX = "rose.multitenancy.async";

    /**
     * Whether to propagate {@code TenantContext} to {@code ThreadPoolTaskExecutor} beans.
     */
    private boolean propagationEnabled = true;

    public boolean isPropagationEnabled() {
        return propagationEnabled;
    }

    public void setPropagationEnabled(boolean propagationEnabled) {
        this.propagationEnabled = propagationEnabled;
    }

}
