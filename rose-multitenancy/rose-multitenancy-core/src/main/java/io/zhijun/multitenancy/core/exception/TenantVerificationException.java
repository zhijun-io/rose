package io.zhijun.multitenancy.core.exception;

/**
 * Thrown when multitenancy verification fails.
 */
public class TenantVerificationException extends IllegalStateException {

    public TenantVerificationException() {
        super("Tenant verification failed");
    }

    public TenantVerificationException(String message) {
        super(message);
    }
}
