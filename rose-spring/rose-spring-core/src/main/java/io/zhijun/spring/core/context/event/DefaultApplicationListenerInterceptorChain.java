package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * {@link ApplicationListenerInterceptor} 链的默认实现。
 */
public class DefaultApplicationListenerInterceptorChain implements ApplicationListenerInterceptorChain {

    private final Iterator<ApplicationListenerInterceptor> iterator;

    private final BiConsumer<ApplicationListener<?>, ApplicationEvent> finalHandler;

    public DefaultApplicationListenerInterceptorChain(Iterable<ApplicationListenerInterceptor> interceptors,
                                                      BiConsumer<ApplicationListener<?>, ApplicationEvent> finalHandler) {
        this.iterator = interceptors.iterator();
        this.finalHandler = finalHandler;
    }

    @Override
    public void intercept(ApplicationListener<?> listener, ApplicationEvent event) {
        while (iterator.hasNext()) {
            ApplicationListenerInterceptor interceptor = iterator.next();
            interceptor.intercept(listener, event, this);
            return;
        }
        finalHandler.accept(listener, event);
    }
}
