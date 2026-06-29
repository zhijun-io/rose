package io.zhijun.multitenancy.core.exception;

import io.zhijun.core.exception.ApplicationException;

/**
 * Thrown when multitenancy verification fails.
 */
public class TenantVerificationException extends ApplicationException {

    private static final String ERROR_CODE = "TENANT_VERIFICATION_FAILED";

    public TenantVerificationException() {
        super(ERROR_CODE, "Tenant verification failed");
    }

    public TenantVerificationException(String message) {
        super(ERROR_CODE, message);
    }
}
