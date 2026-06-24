package io.zhijun.mybatisplus.core.multitenancy;

import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * {@link TenantIdSupplier} backed by {@link TenantContext}.
 */
public final class TenantContextTenantIdSupplier implements TenantIdSupplier {

    @Override
    public String getTenantId() {
        return TenantContext.getRequiredTenantId();
    }

}
