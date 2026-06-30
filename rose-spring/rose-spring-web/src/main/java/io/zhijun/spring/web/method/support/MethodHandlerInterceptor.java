package io.zhijun.spring.web.method.support;

import org.jspecify.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerMethod} 专属的 {@link HandlerInterceptor} 抽象基类。
 *
 * <p>自动处理类型转换，仅当 handler 为 {@link HandlerMethod} 时触发拦截逻辑。</p>
 */
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {

    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                return preHandle(request, response, handlerMethod);
            }
        }
        return true;
    }

    @Override
    public final void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                postHandle(request, response, handlerMethod, modelAndView);
            }
        }
    }

    @Override
    public final void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                      @Nullable Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                afterCompletion(request, response, handlerMethod, ex);
            }
        }
    }

    protected abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                         HandlerMethod handlerMethod) throws Exception;

    protected abstract void postHandle(HttpServletRequest request, HttpServletResponse response,
                                       HandlerMethod handlerMethod, @Nullable ModelAndView modelAndView) throws Exception;

    protected abstract void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                            HandlerMethod handlerMethod, @Nullable Exception ex) throws Exception;

    /**
     * 子类可以重写此方法只对特定 handler 生效。
     */
    protected boolean supports(HttpServletRequest request, HttpServletResponse response,
                               HandlerMethod handlerMethod) throws Exception {
        return true;
    }
}
