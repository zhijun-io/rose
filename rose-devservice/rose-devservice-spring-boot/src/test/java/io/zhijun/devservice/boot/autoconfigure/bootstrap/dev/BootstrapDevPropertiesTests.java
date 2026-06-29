package io.zhijun.devservice.boot.autoconfigure.bootstrap.dev;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BootstrapDevProperties}.
 */
class BootstrapDevPropertiesTests {

    @Test
    void defaultsAndMutators() {
        BootstrapDevProperties properties = new BootstrapDevProperties();

        assertThat(properties.getProfiles()).containsExactly("dev");

        properties.setProfiles(Arrays.asList("local", "dev"));

        assertThat(properties.getProfiles()).containsExactly("local", "dev");
    }
}
