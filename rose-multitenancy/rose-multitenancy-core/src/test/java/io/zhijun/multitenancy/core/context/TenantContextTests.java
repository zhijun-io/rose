package io.zhijun.multitenancy.core.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link TenantContext}.
 */
class TenantContextTests {

  @Test
  void bindRestoresPreviousTenantOnClose() {
    TenantContext.where("outer").run(() -> {
      assertThat(TenantContext.getTenantIdentifier()).isEqualTo("outer");

      try (TenantContext.Scope scope = TenantContext.bind("inner")) {
        assertThat(TenantContext.getTenantIdentifier()).isEqualTo("inner");
      }

      assertThat(TenantContext.getTenantIdentifier()).isEqualTo("outer");
    });
  }

  @Test
  void carrierRunSetsAndRestoresTenant() {
    TenantContext.where("outer").run(() -> {
      assertThat(TenantContext.getTenantIdentifier()).isEqualTo("outer");

      TenantContext.where("inner").run(() -> assertThat(TenantContext.getTenantIdentifier()).isEqualTo("inner"));

      assertThat(TenantContext.getTenantIdentifier()).isEqualTo("outer");
    });

    assertThat(TenantContext.getTenantIdentifier()).isNull();
  }

  @Test
  void carrierCallReturnsValueAndRestoresTenant() throws Exception {
    String result = TenantContext.where("tenant").call(() -> {
      assertThat(TenantContext.getRequiredTenantIdentifier()).isEqualTo("tenant");
      return "ok";
    });

    assertThat(result).isEqualTo("ok");
    assertThat(TenantContext.getTenantIdentifier()).isNull();
  }

  @Test
  void getRequiredTenantIdentifierWhenMissing() {
    assertThatThrownBy(TenantContext::getRequiredTenantIdentifier)
        .isInstanceOf(io.zhijun.multitenancy.core.exceptions.TenantNotFoundException.class);
  }

}
