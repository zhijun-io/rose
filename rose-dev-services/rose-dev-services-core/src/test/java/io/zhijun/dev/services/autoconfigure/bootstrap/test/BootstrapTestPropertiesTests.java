package io.zhijun.dev.services.autoconfigure.bootstrap.test;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BootstrapTestProperties}.
 */
class BootstrapTestPropertiesTests {

    @Test
    void defaultsAndMutators() {
        BootstrapTestProperties properties = new BootstrapTestProperties();

        assertThat(properties.getProfiles()).containsExactly("test");

        properties.setProfiles(Arrays.asList("integration", "test"));

        assertThat(properties.getProfiles()).containsExactly("integration", "test");
    }

}
