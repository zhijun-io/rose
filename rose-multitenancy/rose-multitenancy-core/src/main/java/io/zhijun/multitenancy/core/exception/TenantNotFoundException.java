package io.zhijun.multitenancy.core.exception;

import io.zhijun.core.exception.ApplicationException;

/**
 * Thrown when no multitenancy information is found in a given context.
 */
public class TenantNotFoundException extends ApplicationException {

    public TenantNotFoundException() {
        super(MultitenancyErrorCodes.TENANT_NOT_FOUND, "No tenant found in the current context");
    }

    public TenantNotFoundException(String message) {
        super(MultitenancyErrorCodes.TENANT_NOT_FOUND, message);
    }
}
