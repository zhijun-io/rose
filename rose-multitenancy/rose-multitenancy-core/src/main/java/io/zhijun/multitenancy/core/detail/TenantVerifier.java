package io.zhijun.multitenancy.core.detail;


import io.zhijun.multitenancy.core.exception.TenantVerificationException;

/**
 * Strategy for verifying that a resolved multitenancy identifier is valid and allowed to
 * proceed.
 */

@FunctionalInterface
public interface TenantVerifier {

    /**
     * Verifies the given multitenancy identifier.
     * @throws TenantVerificationException if the multitenancy is invalid or disabled
     */
    void verify(String tenantIdentifier);
}
