package io.zhijun.multitenancy.core.detail;

import io.zhijun.core.annotation.Nullable;

import java.util.List;

/**
 * Loads multitenancy-specific data. It is used throughout the framework as a multitenancy DAO.
 */
public interface TenantDetailsService {

    /**
     * Loads all tenants.
     */
    List<? extends TenantDetails> loadAllTenants();

    /**
     * Loads a multitenancy by tenantIdentifier.
     */
    @Nullable
    TenantDetails loadTenantByIdentifier(String tenantIdentifier);
}
