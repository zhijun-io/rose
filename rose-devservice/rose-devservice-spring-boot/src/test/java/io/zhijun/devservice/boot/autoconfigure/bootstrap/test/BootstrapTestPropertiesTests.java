package io.zhijun.devservice.boot.autoconfigure.bootstrap.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import io.zhijun.devservice.boot.autoconfigure.bootstrap.BootstrapTestProperties;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BootstrapTestProperties}.
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
