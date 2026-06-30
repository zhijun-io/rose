package io.zhijun.multitenancy.core.exception;

import io.zhijun.core.exception.ApplicationException;

/**
 * Thrown when multitenancy verification fails.
 */
public class TenantVerificationException extends ApplicationException {

    public TenantVerificationException() {
        super(MultitenancyErrorCodes.TENANT_VERIFICATION_FAILED, "Tenant verification failed");
    }

    public TenantVerificationException(String message) {
        super(MultitenancyErrorCodes.TENANT_VERIFICATION_FAILED, message);
    }
}
