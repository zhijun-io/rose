package io.zhijun.core.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结构化业务/集成异常，携带 errorCode、retryable 标志和不可变的 details。
 * <p>框架模块决定如何映射到 HTTP 响应、日志或重试策略。
 *
 * <p><b>使用方式</b>：
 * <ul>
 *   <li>{@link #of(String)} — 仅 errorCode，无 message 和 cause</li>
 *   <li>{@link #ApplicationException(String, String, Throwable)} — 完整信息</li>
 *   <li>{@link #retryable()} / {@link #withDetail(String, Object)} — fluent 扩展</li>
 * </ul>
 */
public class ApplicationException extends RuntimeException {

    private final String errorCode;

    private final boolean retryable;

    private final Map<String, Object> details;

    /**
     * 创建仅携带 errorCode 的异常。
     */
    public static ApplicationException of(String errorCode) {
        return new ApplicationException(errorCode, null, null);
    }

    /**
     * 创建携带 errorCode、message、cause 的异常。
     */
    public ApplicationException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, false, Collections.<String, Object>emptyMap());
    }

    /**
     * 返回标记为可重试的新实例。
     */
    public ApplicationException retryable() {
        return new ApplicationException(errorCode, getMessage(), getCause(), true, details);
    }

    /**
     * 返回附加 detail 的新实例。
     */
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

    /**
     * 获取指定 key 的 detail 值（原始类型）。
     */
    public Object getDetail(String key) {
        return details.get(key);
    }

    /**
     * 获取指定 key 的 detail 值并按类型转换，类型不匹配时抛出异常。
     */
    @SuppressWarnings("unchecked")
    public <T> T getDetailAs(String key, Class<T> type) {
        Object value = details.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Detail '" + key + "' is not of required type " + type.getName());
        }
        return (T) value;
    }

    private ApplicationException(
            String errorCode, String message, Throwable cause, boolean retryable, Map<String, Object> details) {
        super(message, cause);
        this.errorCode = ErrorCodes.requireValid(errorCode);
        this.retryable = retryable;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(details));
    }
}
