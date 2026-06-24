package io.zhijun.multitenancy.core.detail;

import java.util.List;

import io.zhijun.core.annotation.Nullable;

/**
 * Loads multitenancy-specific data. It is used throughout the framework as a multitenancy DAO.
 */
public interface TenantDetailsService {

    /**
     * Loads all tenants.
     */
    List<? extends TenantDetails> loadAllTenants();

    /**
     * Loads a multitenancy by identifier.
     */
    @Nullable
    TenantDetails loadTenantByIdentifier(String identifier);

}
