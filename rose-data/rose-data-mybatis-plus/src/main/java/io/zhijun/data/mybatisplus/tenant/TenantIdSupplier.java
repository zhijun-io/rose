package io.zhijun.data.mybatisplus.tenant;

import org.springframework.lang.Nullable;

/**
 * Supplies current tenant identifier for data infrastructure.
 */
@FunctionalInterface
public interface TenantIdSupplier {

    @Nullable
    String getTenantId();
}
