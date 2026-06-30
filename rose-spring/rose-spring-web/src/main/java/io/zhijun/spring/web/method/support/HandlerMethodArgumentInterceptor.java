package io.zhijun.spring.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * HandlerMethod 参数解析拦截器，在参数解析前后提供扩展点。
 */
public interface HandlerMethodArgumentInterceptor {

    default void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception {
    }

    default void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {
    }
}
