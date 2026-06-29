package io.zhijun.mybatisplus.boot.autoconfigure.multitenancy;

import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.mybatisplus.core.multitenancy.TenantIdSupplier;

/**
 * {@link TenantIdSupplier} backed by {@link TenantContext}.
 */
final class TenantContextTenantIdSupplier implements TenantIdSupplier {

    @Override
    public String getTenantId() {
        return TenantContext.getRequiredTenantId();
    }
}
