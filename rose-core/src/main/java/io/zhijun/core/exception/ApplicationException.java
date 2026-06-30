package io.zhijun.core.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结构化业务/集成异常，携带 errorCode、retryable 标志和不可变的 details。
 * <p>框架模块决定如何映射到 HTTP 响应、日志或重试策略。
 */
public class ApplicationException extends RuntimeException {

    private final String errorCode;

    private final boolean retryable;

    private final Map<String, Object> details;

    public ApplicationException(String errorCode) {
        this(errorCode, null, null, false, Collections.emptyMap());
    }

    public ApplicationException(String errorCode, String message) {
        this(errorCode, message, null, false, Collections.emptyMap());
    }

    public ApplicationException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, false, Collections.emptyMap());
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

    public ErrorResponse toErrorResponse() {
        return ErrorResponse.from(this);
    }

    private ApplicationException(
            String errorCode, String message, Throwable cause, boolean retryable, Map<String, Object> details) {
        super(message, cause);
        this.errorCode = ErrorCodes.requireValid(errorCode);
        this.retryable = retryable;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(details));
    }
}
