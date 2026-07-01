package io.zhijun.spring.context.event;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.beans.GenericBeanPostProcessorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asListableBeanFactory;
import static io.zhijun.spring.context.event.InterceptingApplicationEventMulticaster.resolveEventType;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * 拦截式 {@link ApplicationEventMulticaster} 代理。
 * 包装原始 multicast 并注入事件/监听器拦截链。
 */
public class InterceptingApplicationEventMulticasterProxy extends GenericBeanPostProcessorAdapter<ApplicationListener>
        implements ApplicationEventMulticaster, BeanFactoryAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(InterceptingApplicationEventMulticasterProxy.class);

    public static final String DEFAULT_RESET_BEAN_NAME = APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "_ORIGINAL";

    private final String delegateBeanName;

    private ApplicationEventMulticaster delegate;

    private List<ApplicationEventInterceptor> applicationEventInterceptors;

    private List<ApplicationListenerInterceptor> applicationListenerInterceptors;

    private Map<ApplicationListener, InterceptingApplicationListener> applicationListenersMap;

    private Executor taskExecutor;

    public InterceptingApplicationEventMulticasterProxy() {
        this.delegateBeanName = DEFAULT_RESET_BEAN_NAME;
    }

    @Override
    public synchronized void addApplicationListener(ApplicationListener<?> listener) {
        InterceptingApplicationListener interceptingApplicationListener = wrap(listener);
        this.delegate.addApplicationListener(interceptingApplicationListener);
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        this.delegate.addApplicationListenerBean(listenerBeanName);
    }

    @Override
    public synchronized void removeApplicationListener(ApplicationListener<?> listener) {
        final InterceptingApplicationListener wrapper;
        if (isCachedInterceptingApplicationListener(listener)) {
            wrapper = (InterceptingApplicationListener) listener;
        } else {
            wrapper = this.applicationListenersMap.remove(listener);
        }
        if (wrapper != null) {
            this.delegate.removeApplicationListener(wrapper);
        }
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        delegate.removeApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {
        this.delegate.removeApplicationListeners(listener -> {
            if (listener instanceof InterceptingApplicationListener) {
                return predicate.test(((InterceptingApplicationListener) listener).getDelegate());
            }
            return predicate.test(listener);
        });
    }

    @Override
    public void removeApplicationListenerBeans(Predicate<String> predicate) {
        this.delegate.removeApplicationListenerBeans(predicate);
    }

    @Override
    public void removeAllListeners() {
        this.delegate.removeAllListeners();
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        execute(() -> this.delegate.multicastEvent(event));
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        execute(() -> {
            ResolvableType type = resolveEventType(event, eventType);
            java.util.Iterator<ApplicationEventInterceptor> it = applicationEventInterceptors.iterator();
            doIntercept(event, type, it);
        });
    }

    private void doIntercept(ApplicationEvent event, ResolvableType eventType, java.util.Iterator<ApplicationEventInterceptor> it) {
        InterceptingApplicationEventMulticaster.doIntercept(event, eventType, it, this::onEvent);
    }

    @Override
    protected ApplicationListener doPostProcessAfterInitialization(ApplicationListener bean, String beanName) throws BeansException {
        return wrap(bean);
    }

    protected InterceptingApplicationListener wrap(ApplicationListener listener) {
        if (listener instanceof InterceptingApplicationListener) {
            InterceptingApplicationListener interceptingApplicationListener = (InterceptingApplicationListener) listener;
           if (!isCachedInterceptingApplicationListener(listener)) {
                ApplicationListener<?> delegate = interceptingApplicationListener.getDelegate();
                this.applicationListenersMap.put(delegate, interceptingApplicationListener);
            }
            return interceptingApplicationListener;
        } else {
            return this.applicationListenersMap.computeIfAbsent(listener,
                    l -> new InterceptingApplicationListener(l, applicationListenerInterceptors));
        }
    }

    protected boolean isCachedInterceptingApplicationListener(ApplicationListener listener) {
        return this.applicationListenersMap.containsValue(listener);
    }

    private void onEvent(ApplicationEvent event, ResolvableType resolvableType) {
        this.delegate.multicastEvent(event, resolvableType);
    }

    private void execute(Runnable runnable) {
        getTaskExecutor().execute(runnable);
    }

    public void setTaskExecutor(@Nullable Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected Executor getTaskExecutor() {
        if (this.taskExecutor == null) {
            setTaskExecutor(Runnable::run);
        }
        return this.taskExecutor;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ListableBeanFactory listableBeanFactory = asListableBeanFactory(beanFactory);
        this.delegate = beanFactory.getBean(this.delegateBeanName, ApplicationEventMulticaster.class);
        this.applicationEventInterceptors = getSortedBeans(listableBeanFactory, ApplicationEventInterceptor.class);
        this.applicationListenerInterceptors = getSortedBeans(listableBeanFactory, ApplicationListenerInterceptor.class);
        this.applicationListenersMap = new LinkedHashMap<>();
    }

    public Object getDelegate() {
        return this.delegate;
    }

    @Override
    public void destroy() throws Exception {
        this.applicationListenersMap.clear();
        if (this.taskExecutor != null && this.taskExecutor instanceof DisposableBean) {
            ((DisposableBean) this.taskExecutor).destroy();
        }
    }

    private static <T> List<T> getSortedBeans(ListableBeanFactory beanFactory, Class<T> type) {
        List<T> beans = new ArrayList<>(beanFactory.getBeansOfType(type).values());
        AnnotationAwareOrderComparator.sort(beans);
        return beans;
    }
}
