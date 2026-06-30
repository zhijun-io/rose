package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Iterator;
import java.util.function.BiConsumer;

class DefaultApplicationListenerInterceptorChain implements ApplicationListenerInterceptorChain {

    private final Iterator<ApplicationListenerInterceptor> iterator;

    private final BiConsumer<ApplicationListener<?>, ApplicationEvent> listenerAndEventConsumer;

    public DefaultApplicationListenerInterceptorChain(Iterable<ApplicationListenerInterceptor> interceptors,
                                                      BiConsumer<ApplicationListener<?>, ApplicationEvent> listenerAndEventConsumer) {
        this.iterator = interceptors.iterator();
        this.listenerAndEventConsumer = listenerAndEventConsumer;
    }

    @Override
    public void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event) {
        while (iterator.hasNext()) {
            ApplicationListenerInterceptor interceptor = iterator.next();
            interceptor.intercept(applicationListener, event, this);
            return;
        }
        listenerAndEventConsumer.accept(applicationListener, event);
    }
}
