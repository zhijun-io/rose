package io.zhijun.observability.core.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rose observability configuration.
 */
@ConfigurationProperties(prefix = ObservabilityProperties.CONFIG_PREFIX)
public class ObservabilityProperties {

    public static final String CONFIG_PREFIX = "rose.observability";

    private boolean enabled = true;

    private final Conventions conventions = new Conventions();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Conventions getConventions() {
        return conventions;
    }

    public static final class Conventions {

        /**
         * Selected conventions backend id (e.g. {@code opentelemetry}). Empty means auto-select.
         */
        private String backend = "";

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            this.backend = backend;
        }
    }
}
