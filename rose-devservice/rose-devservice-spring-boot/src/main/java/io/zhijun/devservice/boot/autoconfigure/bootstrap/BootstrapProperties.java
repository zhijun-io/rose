package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bootstrap profile configuration.
 */
@ConfigurationProperties(prefix = BootstrapProperties.CONFIG_PREFIX)
public class BootstrapProperties {

    public static final String CONFIG_PREFIX = "rose.bootstrap";

    private final Profiles profiles = new Profiles();

    public Profiles getProfiles() {
        return profiles;
    }

    public static class Profiles {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
