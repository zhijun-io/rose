package io.zhijun.multitenancy.boot.autoconfigure;

import java.util.Arrays;

import io.zhijun.multitenancy.boot.autoconfigure.detail.PropertiesTenantDetailsService;
import io.zhijun.multitenancy.boot.autoconfigure.detail.TenantDetailsProperties;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.detail.TenantDetails;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link PropertiesTenantDetailsService}.
 */
class PropertiesTenantDetailsServiceTests {

    @Test
    void loadAllTenants() {
        TenantDetailsProperties tenantDetailsProperties = new TenantDetailsProperties();
        tenantDetailsProperties.setTenants(Arrays.asList(buildTenantConfig("acme", true), buildTenantConfig("sam", false)));

        PropertiesTenantDetailsService tenantDetailsService = new PropertiesTenantDetailsService(tenantDetailsProperties);
        java.util.List<? extends TenantDetails> tenants = tenantDetailsService.loadAllTenants();

        assertThat(tenants).isNotNull();
        assertThat(tenants).hasSize(2);
    }

    @Test
    void whenTenantEnabledThenReturn() {
        TenantDetailsProperties tenantDetailsProperties = new TenantDetailsProperties();
        tenantDetailsProperties.setTenants(Arrays.asList(buildTenantConfig("acme", true)));

        PropertiesTenantDetailsService tenantDetailsService = new PropertiesTenantDetailsService(tenantDetailsProperties);
        TenantDetails tenant = tenantDetailsService.loadTenantByIdentifier("acme");

        assertThat(tenant).isNotNull();
    }

    @Test
    void whenTenantDisabledThenReturn() {
        TenantDetailsProperties tenantDetailsProperties = new TenantDetailsProperties();
        tenantDetailsProperties.setTenants(Arrays.asList(buildTenantConfig("acme", false)));

        PropertiesTenantDetailsService tenantDetailsService = new PropertiesTenantDetailsService(tenantDetailsProperties);
        TenantDetails tenant = tenantDetailsService.loadTenantByIdentifier("acme");

        assertThat(tenant).isNotNull();
    }

    private TenantDetailsProperties.TenantConfig buildTenantConfig(String identifier, boolean enabled) {
        TenantDetailsProperties.TenantConfig tenantConfig = new TenantDetailsProperties.TenantConfig();
        tenantConfig.setIdentifier(identifier);
        tenantConfig.setEnabled(enabled);
        return tenantConfig;
    }

}
