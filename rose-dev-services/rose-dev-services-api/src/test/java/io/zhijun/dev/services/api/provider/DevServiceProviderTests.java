package io.zhijun.dev.services.api.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DevServiceProvider}.
 */
class DevServiceProviderTests {

  @Test
  void of() {
    DevServiceProvider provider = DevServiceProvider.of("postgresql", "database");

    assertThat(provider.name()).isEqualTo("postgresql");
    assertThat(provider.category()).isEqualTo("database");
  }

}
