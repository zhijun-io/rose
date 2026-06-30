package io.zhijun.devservice.boot.autoconfigure.bootstrap.dev;

import io.zhijun.devservice.boot.autoconfigure.bootstrap.BootstrapDevProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

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
