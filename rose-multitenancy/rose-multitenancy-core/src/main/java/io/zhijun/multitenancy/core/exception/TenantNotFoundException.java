package io.zhijun.multitenancy.core.exception;

import io.zhijun.core.exception.ApplicationException;

/**
 * Thrown when no multitenancy information is found in a given context.
 */
public class TenantNotFoundException extends ApplicationException {

    private static final String ERROR_CODE = "TENANT_NOT_FOUND";

    public TenantNotFoundException() {
        super(ERROR_CODE, "No tenant found in the current context");
    }

    public TenantNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
