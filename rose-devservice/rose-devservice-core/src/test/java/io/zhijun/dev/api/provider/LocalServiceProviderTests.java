package io.zhijun.dev.api.provider;

import io.zhijun.dev.api.provider.LocalServiceProvider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link LocalServiceProvider}.
 */
class LocalServiceProviderTests {

  @Test
  void of() {
    LocalServiceProvider provider = LocalServiceProvider.of("postgresql", "database");

    assertThat(provider.name()).isEqualTo("postgresql");
    assertThat(provider.category()).isEqualTo("database");
  }

}
