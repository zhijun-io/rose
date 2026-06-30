package io.zhijun.spring.webmvc.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.util.StringUtils.hasText;

/**
 * 页面渲染上下文 {@link HandlerInterceptor}，仅在返回页面视图时触发。
 */
public abstract class AbstractPageRenderContextHandlerInterceptor implements HandlerInterceptor {

    @Override
    public final void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        if (isPageRenderRequest(modelAndView)) {
            postHandleOnPageRenderContext(request, response, handler, modelAndView);
        }
    }

    protected abstract void postHandleOnPageRenderContext(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception;

    /**
     * 判断是否为页面渲染请求（{@link ModelAndView#getViewName()} 非空）。
     */
    static boolean isPageRenderRequest(ModelAndView modelAndView) {
        return modelAndView != null && hasText(modelAndView.getViewName());
    }
}
