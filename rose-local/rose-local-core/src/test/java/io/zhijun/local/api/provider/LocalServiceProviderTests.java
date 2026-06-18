package io.zhijun.local.api.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocalServiceProvider}.
 */
class LocalServiceProviderTests {

  @Test
  void of() {
    LocalServiceProvider provider = LocalServiceProvider.of("postgresql", "database");

    assertThat(provider.name()).isEqualTo("postgresql");
    assertThat(provider.category()).isEqualTo("database");
  }

}
