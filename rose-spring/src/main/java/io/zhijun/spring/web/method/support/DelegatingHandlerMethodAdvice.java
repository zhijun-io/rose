package io.zhijun.spring.web.method.support;

import io.zhijun.spring.context.event.OnceApplicationContextEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 委托型 HandlerMethodAdvice，自动从 Spring 容器中发现
 * 其他 {@link HandlerMethodAdvice} Bean（自排除）。
 */
public class DelegatingHandlerMethodAdvice extends OnceApplicationContextEventListener<ContextRefreshedEvent>
        implements HandlerMethodAdvice {

    public static final String BEAN_NAME = "delegatingHandlerMethodAdvice";

    private static final Logger logger = LoggerFactory.getLogger(DelegatingHandlerMethodAdvice.class);

    /** HandlerMethodAdvice beans discovered from the container (excluding self). */
    private List<HandlerMethodAdvice> advices = Collections.emptyList();

    @Override
    protected void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        this.advices = getSortedBeansExcludingSelf(context, HandlerMethodAdvice.class);
        logger.trace("初始化完成: {} 个 HandlerMethodAdvice", this.advices.size());
    }

    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception {
        for (HandlerMethodAdvice advice : this.advices) {
            advice.beforeResolveArgument(parameter, handlerMethod, webRequest);
        }
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {
        for (HandlerMethodAdvice advice : this.advices) {
            advice.afterResolveArgument(parameter, resolvedArgument, handlerMethod, webRequest);
        }
    }

    @Override
    public void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        for (HandlerMethodAdvice advice : this.advices) {
            advice.beforeExecuteMethod(handlerMethod, args, request);
        }
    }

    @Override
    public void afterExecuteMethod(HandlerMethod handlerMethod, Object[] args, @org.jspecify.annotations.Nullable Object returnValue,
                                    @org.jspecify.annotations.Nullable Throwable error, NativeWebRequest request) throws Exception {
        for (HandlerMethodAdvice advice : this.advices) {
            advice.afterExecuteMethod(handlerMethod, args, returnValue, error, request);
        }
    }

    private static <T> List<T> getSortedBeansExcludingSelf(ApplicationContext context, Class<T> type) {
        Map<String, T> beans = context.getBeansOfType(type);
        beans.remove(DelegatingHandlerMethodAdvice.class.getName());
        List<T> sorted = new ArrayList<>(beans.values());
        org.springframework.core.annotation.AnnotationAwareOrderComparator.sort(sorted);
        return sorted;
    }
}
