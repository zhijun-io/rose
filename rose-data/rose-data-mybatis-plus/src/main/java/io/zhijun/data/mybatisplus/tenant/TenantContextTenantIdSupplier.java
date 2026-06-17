package io.zhijun.data.mybatisplus.tenant;

import org.springframework.lang.Nullable;

import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * Reads tenant id from {@link TenantContext}.
 */
public class TenantContextTenantIdSupplier implements TenantIdSupplier {

    @Override
    public @Nullable String getTenantId() {
        return TenantContext.getTenantIdentifier();
    }
}
