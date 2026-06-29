package io.zhijun.multitenancy.core.detail;

import org.apiguardian.api.API;
import io.zhijun.multitenancy.core.exception.TenantVerificationException;

/**
 * Strategy for verifying that a resolved multitenancy identifier is valid and allowed to
 * proceed.
 */
@API(status = API.Status.EXPERIMENTAL)
@FunctionalInterface
public interface TenantVerifier {

    /**
     * Verifies the given multitenancy identifier.
     * @throws TenantVerificationException if the multitenancy is invalid or disabled
     */
    void verify(String tenantIdentifier);
}
