package io.zhijun.devservice.core.autoconfigure.bootstrap;

import io.zhijun.devservice.core.autoconfigure.bootstrap.BootstrapProperties;

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
