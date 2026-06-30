package io.zhijun.mybatisplus.core.multitenancy;

/**
 * Supplies the current multitenancy identifier for MyBatis-Plus multitenancy line filtering.
 */
@FunctionalInterface
public interface TenantIdSupplier {

    /**
     * Returns the multitenancy identifier for the current execution context.
     */
    String getTenantId();
}
