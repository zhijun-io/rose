package io.zhijun.multitenancy.web.autoconfigure;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link HttpTenantResolutionProperties}.
 */
class HttpTenantResolutionPropertiesTests {

  @Test
  void defaultsAndNestedProperties() {
    HttpTenantResolutionProperties properties = new HttpTenantResolutionProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getResolutionMode()).isEqualTo(HttpTenantResolutionProperties.HttpResolutionMode.HEADER);
    assertThat(properties.getHeader().getHeaderName()).isEqualTo("X-TenantId");
    assertThat(properties.getCookie().getCookieName()).isEqualTo("TENANT-ID");
    assertThat(properties.getFilter().getIgnorePaths()).contains("/actuator/**");

    properties.setEnabled(false);
    properties.setResolutionMode(HttpTenantResolutionProperties.HttpResolutionMode.COOKIE);
    properties.getHeader().setHeaderName("Tenant");
    properties.getCookie().setCookieName("tenant");
    properties.getFilter().setIgnorePaths(Collections.singleton("/public/**"));

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getResolutionMode())
        .isEqualTo(HttpTenantResolutionProperties.HttpResolutionMode.COOKIE);
    assertThat(properties.getHeader().getHeaderName()).isEqualTo("Tenant");
    assertThat(properties.getCookie().getCookieName()).isEqualTo("tenant");
    assertThat(properties.getFilter().getIgnorePaths()).containsExactly("/public/**");
  }

}
