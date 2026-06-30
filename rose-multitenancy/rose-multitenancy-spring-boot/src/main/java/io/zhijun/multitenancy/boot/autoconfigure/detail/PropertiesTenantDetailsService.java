package io.zhijun.multitenancy.boot.autoconfigure.detail;

import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import io.zhijun.multitenancy.core.detail.Tenant;
import io.zhijun.multitenancy.core.detail.TenantDetails;
import io.zhijun.multitenancy.core.detail.TenantDetailsService;

/**
 * An implementation of {@link TenantDetailsService} that uses application properties as
 * the source for the multitenancy details.
 */
public final class PropertiesTenantDetailsService implements TenantDetailsService {

    private final TenantDetailsProperties tenantDetailsProperties;

    public PropertiesTenantDetailsService(TenantDetailsProperties tenantDetailsProperties) {
        this.tenantDetailsProperties = tenantDetailsProperties;
    }

    @Override
    public List<? extends TenantDetails> loadAllTenants() {
        return tenantDetailsProperties.getTenants().stream().map(this::toTenant).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public TenantDetails loadTenantByIdentifier(String tenantIdentifier) {
        Assert.hasText(tenantIdentifier, "tenantIdentifier cannot be null or empty");
        return tenantDetailsProperties.getTenants().stream()
                .map(this::toTenant)
                .filter(tenant -> tenant.getIdentifier().equals(tenantIdentifier))
                .findFirst()
                .orElse(null);
    }

    private Tenant toTenant(TenantDetailsProperties.TenantConfig tenantConfig) {
        return Tenant.builder()
                .identifier(tenantConfig.getIdentifier())
                .enabled(tenantConfig.isEnabled())
                .attributes(tenantConfig.getAttributes())
                .build();
    }
}
