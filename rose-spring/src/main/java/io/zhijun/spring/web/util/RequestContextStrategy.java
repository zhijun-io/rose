package io.zhijun.spring.web.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * {@link RequestAttributes 请求上下文} 存储策略枚举。
 *
 * @since 1.0.0
 */
public enum RequestContextStrategy {

    /** 默认策略，保持 Spring 原有存储方式 */
    DEFAULT,

    /** {@link ThreadLocal} 策略 */
    THREAD_LOCAL,

    /** {@link InheritableThreadLocal} 策略 */
    INHERITABLE_THREAD_LOCAL;
}
