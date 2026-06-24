package io.zhijun.devservice.core.autoconfigure.bootstrap.dev;

import java.util.Arrays;

import io.zhijun.devservice.core.autoconfigure.bootstrap.dev.BootstrapDevProperties;

import org.junit.jupiter.api.Test;

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
