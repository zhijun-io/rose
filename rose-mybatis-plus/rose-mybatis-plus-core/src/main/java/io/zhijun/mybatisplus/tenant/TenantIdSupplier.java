package io.zhijun.mybatisplus.tenant;

/**
 * Supplies the current tenant identifier for MyBatis-Plus tenant line filtering.
 */
@FunctionalInterface
public interface TenantIdSupplier {

    /**
     * Returns the tenant identifier for the current execution context.
     */
    String getTenantId();

}
