package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BootstrapProperties}.
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
