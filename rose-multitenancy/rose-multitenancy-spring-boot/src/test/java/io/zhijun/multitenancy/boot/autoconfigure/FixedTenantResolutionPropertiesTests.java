package io.zhijun.multitenancy.boot.autoconfigure;

import io.zhijun.multitenancy.boot.autoconfigure.web.FixedTenantResolutionProperties;

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
    assertThat(properties.getTenantId()).isEqualTo("default");

    properties.setEnabled(true);
    properties.setTenantId("acme");

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getTenantId()).isEqualTo("acme");
  }

}
