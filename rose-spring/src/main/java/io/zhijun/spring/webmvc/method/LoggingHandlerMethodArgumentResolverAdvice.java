package io.zhijun.spring.webmvc.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 日志记录 {@link HandlerMethodArgumentResolverAdvice}，在 TRACE 级别记录参数解析过程。
 */
public class LoggingHandlerMethodArgumentResolverAdvice implements HandlerMethodArgumentResolverAdvice {

    private static final Logger logger = LoggerFactory.getLogger(LoggingHandlerMethodArgumentResolverAdvice.class);

    @Override
    public void beforeResolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        if (logger.isTraceEnabled()) {
            logger.trace("beforeResolveArgument - parameter : {} , mavContainer : {} , webRequest : {} , binderFactory : {}",
                    parameter, mavContainer, webRequest, binderFactory);
        }
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        if (logger.isTraceEnabled()) {
            logger.trace("afterResolveArgument - parameter : {} , resolvedArgument : {} , mavContainer : {} , webRequest : {} , binderFactory : {}",
                    parameter, resolvedArgument, mavContainer, webRequest, binderFactory);
        }
    }
}
