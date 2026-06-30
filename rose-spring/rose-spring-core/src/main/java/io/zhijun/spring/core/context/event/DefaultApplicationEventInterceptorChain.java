package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * {@link ApplicationEventInterceptor} 链的默认实现。
 */
public class DefaultApplicationEventInterceptorChain implements ApplicationEventInterceptorChain {

    private final Iterator<ApplicationEventInterceptor> iterator;

    private final BiConsumer<ApplicationEvent, ResolvableType> finalHandler;

    public DefaultApplicationEventInterceptorChain(Iterable<ApplicationEventInterceptor> interceptors,
                                                   BiConsumer<ApplicationEvent, ResolvableType> finalHandler) {
        this.iterator = interceptors.iterator();
        this.finalHandler = finalHandler;
    }

    @Override
    public void intercept(ApplicationEvent event, ResolvableType eventType) {
        while (iterator.hasNext()) {
            ApplicationEventInterceptor interceptor = iterator.next();
            interceptor.intercept(event, eventType, this);
            return;
        }
        finalHandler.accept(event, eventType);
    }
}
