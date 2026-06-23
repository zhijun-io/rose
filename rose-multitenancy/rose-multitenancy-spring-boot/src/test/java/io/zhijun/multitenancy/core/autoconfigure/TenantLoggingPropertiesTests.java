package io.zhijun.multitenancy.core.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link TenantLoggingProperties}.
 */
class TenantLoggingPropertiesTests {

  @Test
  void defaultsAndMutators() {
    TenantLoggingProperties properties = new TenantLoggingProperties();

    assertThat(properties.getMdc().isEnabled()).isTrue();
    assertThat(properties.getMdc().getKeyName()).isEqualTo("tenantId");

    properties.getMdc().setEnabled(false);
    properties.getMdc().setKeyName("tenant.id");

    assertThat(properties.getMdc().isEnabled()).isFalse();
    assertThat(properties.getMdc().getKeyName()).isEqualTo("tenant.id");
  }

}
