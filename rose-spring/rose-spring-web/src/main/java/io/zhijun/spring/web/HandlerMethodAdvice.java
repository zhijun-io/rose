package io.zhijun.spring.web;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * HandlerMethod 拦截门面接口，在参数解析和方法执行前后提供扩展点。
 *
 * <p>包含 4 个生命周期钩子：参数解析前/后、方法执行前/后。</p>
 */
public interface HandlerMethodAdvice {

    default void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception {
    }

    default void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {
    }

    default void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
    }

    default void afterExecuteMethod(HandlerMethod handlerMethod, Object[] args, @Nullable Object returnValue,
                                    @Nullable Throwable error, NativeWebRequest request) throws Exception {
    }
}
