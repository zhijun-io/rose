package io.zhijun.multitenancy.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.boot.autoconfigure.detail.TenantDetailsProperties;

/**
 * Unit test for {@link TenantDetailsProperties}.
 */
class TenantDetailsPropertiesTests {

    @Test
    void defaultsAndMutators() {
        TenantDetailsProperties properties = new TenantDetailsProperties();

        assertThat(properties.getSource()).isEqualTo(TenantDetailsProperties.Source.NONE);
        assertThat(properties.getTenants()).isEmpty();

        TenantDetailsProperties.TenantConfig tenant = new TenantDetailsProperties.TenantConfig();
        tenant.setIdentifier("acme");
        tenant.setEnabled(false);
        tenant.setAttributes(Collections.<String, Object>singletonMap("region", "eu"));

        properties.setSource(TenantDetailsProperties.Source.PROPERTIES);
        properties.setTenants(Collections.singletonList(tenant));

        assertThat(properties.getSource()).isEqualTo(TenantDetailsProperties.Source.PROPERTIES);
        assertThat(properties.getTenants()).hasSize(1);
        assertThat(properties.getTenants().get(0).getIdentifier()).isEqualTo("acme");
        assertThat(properties.getTenants().get(0).isEnabled()).isFalse();
        assertThat(properties.getTenants().get(0).getAttributes()).containsEntry("region", "eu");
    }

    @Test
    void tenantConfigRequiresIdentifier() {
        TenantDetailsProperties.TenantConfig tenant = new TenantDetailsProperties.TenantConfig();

        assertThatThrownBy(() -> tenant.setIdentifier(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
