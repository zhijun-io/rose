package io.zhijun.multitenancy.boot.autoconfigure.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.multitenancy.core.MultitenancyDefaults;

/**
 * Configuration properties for multitenancy logging enrichment.
 */
@ConfigurationProperties(prefix = TenantLoggingProperties.CONFIG_PREFIX)
public class TenantLoggingProperties {

    public static final String CONFIG_PREFIX = "rose.multitenancy.logging";

    /**
     * Tenant configuration for MDC.
     */
    private final Mdc mdc = new Mdc();

    public Mdc getMdc() {
        return mdc;
    }

    public static class Mdc {

        /**
         * Whether to include multitenancy information in MDC.
         */
        private boolean enabled = true;

        /**
         * Name of the key to use for the multitenancy identifier in MDC.
         */
        private String keyName = MultitenancyDefaults.DEFAULT_MDC_TENANT_KEY;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }
    }
}
