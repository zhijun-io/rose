package io.zhijun.core.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Framework-agnostic error response generated from {@link ApplicationException}.
 *
 * <p>The model follows the same intent as {@link ApplicationException}: keep stable,
 * machine-readable failure metadata in core and let framework modules decide transport
 * details such as HTTP status, headers and serialization.
 */
public final class ErrorResponse {

    private final String errorCode;

    private final String message;

    private final boolean retryable;

    private final Map<String, Object> details;

    public ErrorResponse(String errorCode, String message, boolean retryable, Map<String, Object> details) {
        this.errorCode = ErrorCodes.requireValid(errorCode);
        this.message = message;
        this.retryable = retryable;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(
                details == null ? Collections.<String, Object>emptyMap() : details));
    }

    public static ErrorResponse from(ApplicationException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        return new ErrorResponse(
                exception.getErrorCode(), exception.getMessage(), exception.isRetryable(), exception.getDetails());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
