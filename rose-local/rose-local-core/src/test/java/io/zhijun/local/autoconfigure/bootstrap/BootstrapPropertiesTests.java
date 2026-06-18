package io.zhijun.local.autoconfigure.bootstrap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BootstrapProperties}.
 */
class BootstrapPropertiesTests {

    @Test
    void defaultsAndMutators() {
        BootstrapProperties properties = new BootstrapProperties();

        assertThat(properties.getProfiles().isEnabled()).isTrue();

        properties.getProfiles().setEnabled(false);

        assertThat(properties.getProfiles().isEnabled()).isFalse();
    }

}
