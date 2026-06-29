package io.zhijun.core.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured application exception for business and integration failures.
 *
 * <p>The shape is inspired by OpenRewrite's practice of carrying stable,
 * machine-readable failure information with exceptions. Rose keeps that metadata in core,
 * while framework modules decide how to map it to HTTP responses, logs, metrics, traces
 * or retry policies.
 *
 * <p>Currently provides:
 * <ul>
 *     <li>a stable {@link #getErrorCode() error code} for programmatic handling</li>
 *     <li>a {@link #isRetryable() retryable} flag for upper-layer retry frameworks</li>
 *     <li>immutable {@link #getDetails() details} for structured context</li>
 *     <li>typed {@link #getDetail(String, Class)} detail access for callers</li>
 * </ul>
 *
 * <p>TODO:
 * <ul>
 *     <li>support standardized detail keys and machine-readable metadata</li>
 *     <li>integrate with framework-specific exception mapping and observability conventions</li>
 * </ul>
 */
public class ApplicationException extends RuntimeException {

    private final String errorCode;

    private final boolean retryable;

    private final Map<String, Object> details;

    public ApplicationException(String errorCode) {
        this(errorCode, null, null, false, Collections.<String, Object>emptyMap());
    }

    public ApplicationException(String errorCode, String message) {
        this(errorCode, message, null, false, Collections.<String, Object>emptyMap());
    }

    public ApplicationException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, false, Collections.<String, Object>emptyMap());
    }

    public ApplicationException retryable() {
        return new ApplicationException(errorCode, getMessage(), getCause(), true, details);
    }

    public ApplicationException withDetail(String key, Object value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("detail key cannot be null or empty");
        }
        Map<String, Object> updatedDetails = new LinkedHashMap<String, Object>(details);
        updatedDetails.put(key, value);
        return new ApplicationException(errorCode, getMessage(), getCause(), retryable, updatedDetails);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public <T> T getDetail(String key, Class<T> type) {
        Object value = details.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Detail '" + key + "' is not of required type " + type.getName());
        }
        return type.cast(value);
    }

    private ApplicationException(
            String errorCode, String message, Throwable cause, boolean retryable, Map<String, Object> details) {
        super(message, cause);
        this.errorCode = ErrorCodes.requireValid(errorCode);
        this.retryable = retryable;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(details));
    }
}
