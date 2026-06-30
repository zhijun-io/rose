package io.zhijun.spring.webmvc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Lazy {@link HandlerInterceptor} that is composited by {@link HandlerInterceptor} beans with the specified types
 *
 * @see HandlerInterceptor
 * @since 1.0.0
 */
public class LazyCompositeHandlerInterceptor implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware, HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LazyCompositeHandlerInterceptor.class);

    /**
     * The bean name of {@link LazyCompositeHandlerInterceptor}
     */
    public static final String BEAN_NAME = "lazyCompositeHandlerInterceptor";

    private final Set<Class<? extends HandlerInterceptor>> interceptorClasses;

    private List<HandlerInterceptor> interceptors = Collections.emptyList();

    private ApplicationContext applicationContext;

    public LazyCompositeHandlerInterceptor(Class<? extends HandlerInterceptor>... interceptorClasses) {
        Set<Class<? extends HandlerInterceptor>> classes = new LinkedHashSet<Class<? extends HandlerInterceptor>>();
        Collections.addAll(classes, interceptorClasses);
        this.interceptorClasses = Collections.unmodifiableSet(classes);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!Objects.equals(applicationContext, event.getSource())) {
            return;
        }
        List<HandlerInterceptor> allInterceptors = new LinkedList<HandlerInterceptor>();
        for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
            Collection<? extends HandlerInterceptor> beans = applicationContext.getBeansOfType(interceptorClass).values();
            for (HandlerInterceptor interceptor : beans) {
                if (interceptor != this) {
                    allInterceptors.add(interceptor);
                }
            }
        }
        AnnotationAwareOrderComparator.sort(allInterceptors);
        this.interceptors = allInterceptors;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.afterCompletion(request, response, handler, ex);
        }
    }
}
