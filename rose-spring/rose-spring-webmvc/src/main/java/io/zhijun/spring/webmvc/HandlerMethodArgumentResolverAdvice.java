package io.zhijun.spring.webmvc;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} 的 advice 接口，在参数解析前后提供扩展点。
 */
public interface HandlerMethodArgumentResolverAdvice {

    void beforeResolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                               NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception;

    void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, @Nullable ModelAndViewContainer mavContainer,
                              NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception;
}
