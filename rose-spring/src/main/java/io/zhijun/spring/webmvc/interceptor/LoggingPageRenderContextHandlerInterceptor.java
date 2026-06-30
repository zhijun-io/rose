package io.zhijun.spring.webmvc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志记录 {@link AbstractPageRenderContextHandlerInterceptor}，在 TRACE 级别记录页面渲染上下文。
 */
public class LoggingPageRenderContextHandlerInterceptor extends AbstractPageRenderContextHandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPageRenderContextHandlerInterceptor.class);

    @Override
    protected void postHandleOnPageRenderContext(HttpServletRequest request, HttpServletResponse response,
                                                 Object handler, ModelAndView modelAndView) {
        if (logger.isTraceEnabled()) {
            logger.trace("The handler : {} , modelAndView : {}", handler, modelAndView);
        }
    }
}
