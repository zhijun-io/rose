package io.zhijun.observation.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rose observation configuration.
 */
@ConfigurationProperties(prefix = ObservationProperties.CONFIG_PREFIX)
public class ObservationProperties {

    public static final String CONFIG_PREFIX = "rose.observation";

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
