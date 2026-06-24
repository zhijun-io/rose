package io.zhijun.multitenancy.core.context;

import io.zhijun.multitenancy.core.exception.TenantNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link TenantContext}.
 */
class TenantContextTests {

  @Test
  void bindRestoresPreviousTenantOnClose() {
    TenantContext.where("outer").run(() -> {
      assertThat(TenantContext.getTenantId()).isEqualTo("outer");

      try (TenantContext.Scope scope = TenantContext.bind("inner")) {
        assertThat(TenantContext.getTenantId()).isEqualTo("inner");
      }

      assertThat(TenantContext.getTenantId()).isEqualTo("outer");
    });
  }

  @Test
  void carrierRunSetsAndRestoresTenant() {
    TenantContext.where("outer").run(() -> {
      assertThat(TenantContext.getTenantId()).isEqualTo("outer");

      TenantContext.where("inner").run(() -> assertThat(TenantContext.getTenantId()).isEqualTo("inner"));

      assertThat(TenantContext.getTenantId()).isEqualTo("outer");
    });

    assertThat(TenantContext.getTenantId()).isNull();
  }

  @Test
  void carrierCallReturnsValueAndRestoresTenant() throws Exception {
    String result = TenantContext.where("multitenancy").call(() -> {
      assertThat(TenantContext.getRequiredTenantId()).isEqualTo("multitenancy");
      return "ok";
    });

    assertThat(result).isEqualTo("ok");
    assertThat(TenantContext.getTenantId()).isNull();
  }

  @Test
  void getRequiredTenantIdWhenMissing() {
    assertThatThrownBy(TenantContext::getRequiredTenantId)
        .isInstanceOf(TenantNotFoundException.class);
  }

}
