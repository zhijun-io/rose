package io.zhijun.multitenancy.autoconfigure;

import io.zhijun.multitenancy.autoconfigure.core.FixedTenantResolutionProperties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link FixedTenantResolutionProperties}.
 */
class FixedTenantResolutionPropertiesTests {

  @Test
  void defaultsAndMutators() {
    FixedTenantResolutionProperties properties = new FixedTenantResolutionProperties();

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getTenantIdentifier()).isEqualTo("default");

    properties.setEnabled(true);
    properties.setTenantIdentifier("acme");

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getTenantIdentifier()).isEqualTo("acme");
  }

}
