package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Objects;

import static org.springframework.util.Assert.notNull;

/**
 * 拦截式 {@link ApplicationListener} 包装器。
 */
class InterceptingApplicationListener implements GenericApplicationListenerAdapter {

    private final ApplicationListener<?> delegate;

    private final GenericApplicationListener smartListener;

    private final List<ApplicationListenerInterceptor> interceptors;

    InterceptingApplicationListener(ApplicationListener<?> listener, List<ApplicationListenerInterceptor> interceptors) {
        notNull(listener, "The 'listener' argument must not be null");
        ApplicationListener<?> delegate = getDelegate(listener);
        this.delegate = delegate;
        this.smartListener = (delegate instanceof GenericApplicationListener ?
                (GenericApplicationListener) delegate : new org.springframework.context.event.GenericApplicationListenerAdapter(delegate));
        this.interceptors = interceptors;
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return smartListener.supportsEventType(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(this.interceptors, this::onEvent);
        chain.intercept(delegate, event);
    }

    private void onEvent(ApplicationListener applicationListener, ApplicationEvent event) {
        applicationListener.onApplicationEvent(event);
    }

    public ApplicationListener<?> getDelegate() {
        return this.delegate;
    }

    static ApplicationListener<?> getDelegate(ApplicationListener<?> listener) {
        ApplicationListener delegate = listener;
        while (delegate instanceof InterceptingApplicationListener) {
            delegate = ((InterceptingApplicationListener) delegate).delegate;
       }
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterceptingApplicationListener)) return false;
        return getDelegate().equals(((InterceptingApplicationListener) o).getDelegate());
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

}
