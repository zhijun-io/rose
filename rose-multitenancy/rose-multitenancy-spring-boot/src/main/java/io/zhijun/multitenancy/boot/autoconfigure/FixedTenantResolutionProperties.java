package io.zhijun.multitenancy.boot.autoconfigure.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for fixed multitenancy resolution.
 */
@ConfigurationProperties(prefix = FixedTenantResolutionProperties.CONFIG_PREFIX)
public class FixedTenantResolutionProperties {

    public static final String CONFIG_PREFIX = "rose.multitenancy.resolution.fixed";

    /**
     * Whether a fixed multitenancy resolution strategy should be used.
     */
    private boolean enabled = false;

    /**
     * Identifier of the fixed multitenancy to use in each context.
     */
    private String tenantId = "default";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}
