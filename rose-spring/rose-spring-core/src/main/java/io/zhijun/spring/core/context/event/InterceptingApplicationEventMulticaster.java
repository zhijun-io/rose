package io.zhijun.spring.core.context.event;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 基于 {@link SimpleApplicationEventMulticaster} 的可拦截事件多播器。
 *
 * <p>从 Spring 容器中自动发现 {@link ApplicationEventInterceptor} 和
 * {@link ApplicationListenerInterceptor} 的 Bean，并在事件广播时执行拦截链。</p>
 */
public class InterceptingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private List<ApplicationEventInterceptor> eventInterceptors;

    private List<ApplicationListenerInterceptor> listenerInterceptors;

    @Override
    public final void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType) {
        execute(() -> {
            ResolvableType type = eventType != null ? eventType : resolveDefaultEventType(event);
            DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(
                    this.eventInterceptors, this::doMulticastEvent);
            chain.intercept(event, type);
        });
    }

    @Override
    protected final void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(
                this.listenerInterceptors, this::doInvokeListener);
        chain.intercept(listener, event);
    }

    protected void doMulticastEvent(ApplicationEvent event, ResolvableType eventType) {
        super.multicastEvent(event, eventType);
    }

    protected void doInvokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        super.invokeListener(listener, event);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
            this.eventInterceptors = getSortedBeans(lbf, ApplicationEventInterceptor.class);
            this.listenerInterceptors = getSortedBeans(lbf, ApplicationListenerInterceptor.class);
        }
    }

    @Override
    protected Executor getTaskExecutor() {
        Executor executor = super.getTaskExecutor();
        if (executor == null) {
            executor = Runnable::run;
        }
        return executor;
    }

    private void execute(Runnable runnable) {
        getTaskExecutor().execute(runnable);
    }

    static ResolvableType resolveDefaultEventType(ApplicationEvent event) {
        return ResolvableType.forInstance(event);
    }

    private static <T> List<T> getSortedBeans(ListableBeanFactory beanFactory, Class<T> type) {
        Map<String, T> beans = beanFactory.getBeansOfType(type);
        List<T> sorted = new ArrayList<>(beans.values());
        org.springframework.core.annotation.AnnotationAwareOrderComparator.sort(sorted);
        return sorted;
    }
}
