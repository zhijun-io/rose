package io.zhijun.spring.web;

import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * HandlerMethod 执行拦截器，在方法执行前后提供扩展点。
 */
public interface HandlerMethodInterceptor {

    default void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
    }

    default void afterExecute(HandlerMethod handlerMethod, Object[] args, @Nullable Object returnValue,
                              @Nullable Throwable error, NativeWebRequest request) throws Exception {
    }
}
