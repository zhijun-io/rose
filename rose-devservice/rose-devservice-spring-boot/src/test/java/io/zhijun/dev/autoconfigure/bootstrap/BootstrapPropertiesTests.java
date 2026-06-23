package io.zhijun.dev.autoconfigure.bootstrap;

import io.zhijun.dev.autoconfigure.bootstrap.BootstrapProperties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
