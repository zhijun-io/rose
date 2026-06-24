package io.zhijun.multitenancy.core.context;

import io.zhijun.core.annotation.Incubating;

/**
 * Strategy to use a fixed value as the current multitenancy, regardless of the source context.
 */
@Incubating
public final class FixedTenantResolver implements TenantResolver<Object> {

    public static final String DEFAULT_FIXED_TENANT_IDENTIFIER = "default";

    private final String fixedTenantIdentifier;

    public FixedTenantResolver() {
        fixedTenantIdentifier = DEFAULT_FIXED_TENANT_IDENTIFIER;
    }

    public FixedTenantResolver(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("tenantIdentifier cannot be null or empty");
        }
        this.fixedTenantIdentifier = tenantIdentifier;
    }

    @Override
    public String resolveTenantId(Object source) {
        return fixedTenantIdentifier;
    }

}
