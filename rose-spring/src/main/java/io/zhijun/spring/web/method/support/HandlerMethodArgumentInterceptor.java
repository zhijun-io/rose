package io.zhijun.spring.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * {@link HandlerMethod} 参数的拦截器 SPI。
 *
 * @since 1.0.0
 */
public interface HandlerMethodArgumentInterceptor {

    /**
     * 参数解析前回调。
     */
    default void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception {
    }

    /**
     * 参数解析后回调。
     */
    default void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {
    }
}
