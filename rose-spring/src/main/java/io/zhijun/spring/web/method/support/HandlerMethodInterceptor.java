package io.zhijun.spring.web.method.support;

import io.zhijun.core.annotation.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * {@link HandlerMethod} 执行拦截器 SPI。
 *
 * @since 1.0.0
 */
public interface HandlerMethodInterceptor {

    /**
     * 方法执行前回调。
     */
    default void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
    }

    /**
     * 方法执行后回调（成功或异常）。
     */
    default void afterExecute(HandlerMethod handlerMethod, Object[] args, @Nullable Object returnValue,
                              @Nullable Throwable error, NativeWebRequest request) throws Exception {
    }
}
