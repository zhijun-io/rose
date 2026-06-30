package io.zhijun.spring.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import io.zhijun.spring.core.context.OnceApplicationContextEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 委托型 HandlerMethodAdvice，自动从 Spring 容器中发现
 * {@link HandlerMethodArgumentInterceptor} 和 {@link HandlerMethodInterceptor} 的 Bean。
 */
public class DelegatingHandlerMethodAdvice extends OnceApplicationContextEventListener<ContextRefreshedEvent>
        implements HandlerMethodAdvice {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingHandlerMethodAdvice.class);

    private List<HandlerMethodArgumentInterceptor> argumentInterceptors = Collections.emptyList();

    private List<HandlerMethodInterceptor> methodInterceptors = Collections.emptyList();

    @Override
    protected void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        this.argumentInterceptors = getSortedBeans(context, HandlerMethodArgumentInterceptor.class);
        this.methodInterceptors = getSortedBeans(context, HandlerMethodInterceptor.class);
        logger.trace("初始化完成: {} 个 HandlerMethodArgumentInterceptor, {} 个 HandlerMethodInterceptor",
                this.argumentInterceptors.size(), this.methodInterceptors.size());
    }

    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception {
        for (HandlerMethodArgumentInterceptor interceptor : this.argumentInterceptors) {
            interceptor.beforeResolveArgument(parameter, handlerMethod, webRequest);
        }
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {
        for (HandlerMethodArgumentInterceptor interceptor : this.argumentInterceptors) {
            interceptor.afterResolveArgument(parameter, resolvedArgument, handlerMethod, webRequest);
        }
    }

    @Override
    public void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        for (HandlerMethodInterceptor interceptor : this.methodInterceptors) {
            interceptor.beforeExecute(handlerMethod, args, request);
        }
    }

    @Override
    public void afterExecuteMethod(HandlerMethod handlerMethod, Object[] args, @org.jspecify.annotations.Nullable Object returnValue,
                                    @org.jspecify.annotations.Nullable Throwable error, NativeWebRequest request) throws Exception {
        for (HandlerMethodInterceptor interceptor : this.methodInterceptors) {
            interceptor.afterExecute(handlerMethod, args, returnValue, error, request);
        }
    }

    private static <T> List<T> getSortedBeans(ApplicationContext context, Class<T> type) {
        Map<String, T> beans = context.getBeansOfType(type);
        List<T> sorted = new ArrayList<>(beans.values());
        org.springframework.core.annotation.AnnotationAwareOrderComparator.sort(sorted);
        return sorted;
    }
}
