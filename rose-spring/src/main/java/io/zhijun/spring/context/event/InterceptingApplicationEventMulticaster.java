package io.zhijun.spring.context.event;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link ApplicationEventMulticaster} that wraps {@link SimpleApplicationEventMulticaster}
 * and applies the {@link ApplicationEventInterceptor} chain before dispatching events.
 * <p>
 * (借鉴 microsphere-spring {@code InterceptingApplicationEventMulticaster})
 *
 * @see ApplicationEventInterceptor
 */
public class InterceptingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private List<ApplicationEventInterceptor> applicationEventInterceptors = Collections.emptyList();

    @Override
    public final void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = resolveEventType(event, eventType);
        Iterator<ApplicationEventInterceptor> it = applicationEventInterceptors.iterator();
        doIntercept(event, type, it);
    }

    private void doIntercept(ApplicationEvent event, ResolvableType eventType, Iterator<ApplicationEventInterceptor> it) {
        if (it.hasNext()) {
            it.next().intercept(event, eventType, (e, t) -> doIntercept(e, t, it));
        } else {
            doMulticastEvent(event, eventType);
        }
    }

    static ResolvableType resolveEventType(ApplicationEvent event, ResolvableType eventType) {
        return eventType != null ? eventType : ResolvableType.forInstance(event);
    }

    protected void doMulticastEvent(ApplicationEvent event, ResolvableType eventType) {
        super.multicastEvent(event, eventType);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listable = (ListableBeanFactory) beanFactory;
            this.applicationEventInterceptors = resolveSortedBeans(listable, ApplicationEventInterceptor.class);
        }
    }

    private static <T> List<T> resolveSortedBeans(ListableBeanFactory beanFactory, Class<T> type) {
        List<T> beans = new ArrayList<T>(beanFactory.getBeansOfType(type).values());
        Collections.sort(beans, AnnotationAwareOrderComparator.INSTANCE);
        return beans;
    }
}
