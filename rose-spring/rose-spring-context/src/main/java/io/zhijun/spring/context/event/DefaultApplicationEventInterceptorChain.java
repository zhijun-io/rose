package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Default implementation of {@link ApplicationEventInterceptorChain}.
 * <p>
 * Iterates through interceptors and calls the final consumer when none remain.
 * <p>
 * (借鉴 microsphere-spring {@code DefaultApplicationEventInterceptorChain})
 *
 * @see ApplicationEventInterceptor
 * @see ApplicationEventInterceptorChain
 */
class DefaultApplicationEventInterceptorChain implements ApplicationEventInterceptorChain {

    private final Iterator<ApplicationEventInterceptor> iterator;

    private final BiConsumer<ApplicationEvent, ResolvableType> terminalConsumer;

    public DefaultApplicationEventInterceptorChain(Iterable<ApplicationEventInterceptor> interceptors,
                                                   BiConsumer<ApplicationEvent, ResolvableType> terminalConsumer) {
        this.iterator = interceptors.iterator();
        this.terminalConsumer = terminalConsumer;
    }

    @Override
    public void intercept(ApplicationEvent event, ResolvableType eventType) {
        if (iterator.hasNext()) {
            ApplicationEventInterceptor interceptor = iterator.next();
            interceptor.intercept(event, eventType, this);
        } else {
            terminalConsumer.accept(event, eventType);
        }
    }
}
