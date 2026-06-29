package io.zhijun.core.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Error code conventions for {@link ApplicationException}.
 */
public final class ErrorCodes {

    /**
     * Error codes are stable machine-readable identifiers, for example {@code TENANT_NOT_FOUND}.
     */
    public static final String PATTERN = "^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$";

    private ErrorCodes() {}

    public static String requireValid(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            throw new IllegalArgumentException("errorCode cannot be null or empty");
        }
        if (!errorCode.matches(PATTERN)) {
            throw new IllegalArgumentException("errorCode must match " + PATTERN + ": " + errorCode);
        }
        return errorCode;
    }
}
