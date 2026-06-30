package io.zhijun.core.exception;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

/**
 * Error code 命名规范和通用常量。
 */
public final class ErrorCodes {

    /**
     * 跨模块共享的通用错误码。
     */
    public static final class Common {
        public static final String NOT_FOUND        = "NOT_FOUND";
        public static final String BAD_REQUEST      = "BAD_REQUEST";
        public static final String CONFLICT         = "CONFLICT";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String INTERNAL_ERROR   = "INTERNAL_ERROR";
        private Common() {}
    }

    /**
     * Error code 格式：稳定机器可读标识符，例如 {@code TENANT_NOT_FOUND}。
     */
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$");
    static final String PATTERN = "^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$";

    private ErrorCodes() {}

    public static String requireValid(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            throw new IllegalArgumentException("errorCode cannot be null or empty");
        }
        if (!ERROR_CODE_PATTERN.matcher(errorCode).matches()) {
            throw new IllegalArgumentException("errorCode must match " + PATTERN + ": " + errorCode);
        }
        return errorCode;
    }
}
