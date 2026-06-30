package io.zhijun.spring.webmvc.interceptor;

import io.zhijun.spring.web.MethodHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志记录 {@link MethodHandlerInterceptor}，在 TRACE 级别记录 HandlerMethod 调用。
 */
public class LoggingMethodHandlerInterceptor extends MethodHandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMethodHandlerInterceptor.class);

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        if (logger.isTraceEnabled()) {
            logger.trace("preHandle - handlerMethod : {}", handlerMethod);
        }
        return true;
    }

    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                              ModelAndView modelAndView) {
        if (logger.isTraceEnabled()) {
            logger.trace("postHandle - handlerMethod : {} , modelAndView : {}", handlerMethod, modelAndView);
        }
    }

    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   HandlerMethod handlerMethod, Exception ex) {
        if (logger.isTraceEnabled()) {
            logger.trace("afterCompletion - handlerMethod : {} , exception : {}", handlerMethod, ex);
        }
    }

    @Override
    protected boolean supports(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        return request != null;
    }
}
