package io.zhijun.spring.beans.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import io.zhijun.core.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor,
        BeanNameAware {

    private static final Logger logger = LoggerFactory.getLogger(ListenableAutowireCandidateResolver.class);

    private AutowireCandidateResolver delegate;

    private List<AutowireCandidateResolvingListener> listeners = Collections.emptyList();

    private String beanName;

    public void addListener(AutowireCandidateResolvingListener one, AutowireCandidateResolvingListener... more) {
        int len = more.length;
        AutowireCandidateResolvingListener[] combined = new AutowireCandidateResolvingListener[len + 1];
        combined[0] = one;
        System.arraycopy(more, 0, combined, 1, len);
        addListeners(combined);
    }

    public void addListeners(AutowireCandidateResolvingListener[] listeners) {
        addListeners(Arrays.asList(listeners));
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        if (this.listeners.isEmpty()) {
            this.listeners = new ArrayList<>(listeners);
        } else {
            this.listeners.addAll(listeners);
        }
        AnnotationAwareOrderComparator.sort(this.listeners);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        return delegate.isRequired(descriptor);
    }

    @Override
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        return delegate.hasQualifier(descriptor);
    }

    @Nullable
    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object suggestedValue = delegate.getSuggestedValue(descriptor);
        listeners.forEach(l -> l.suggestedValueResolved(descriptor, suggestedValue));
        return suggestedValue;
    }

    @Nullable
    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Object proxy = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        listeners.forEach(l -> l.lazyProxyResolved(descriptor, beanName, proxy));
        return proxy;
    }

    @Override
    public AutowireCandidateResolver cloneIfNecessary() {
        return delegate.cloneIfNecessary();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        wrap(beanFactory);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void wrap(BeanFactory beanFactory) {
        if (!(beanFactory instanceof DefaultListableBeanFactory)) {
            logger.warn("BeanFactory {} is not a DefaultListableBeanFactory, cannot wrap", beanFactory);
            return;
        }
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) beanFactory;
        AutowireCandidateResolver autowireCandidateResolver = dbf.getAutowireCandidateResolver();
        if (autowireCandidateResolver != this) {
            List<AutowireCandidateResolvingListener> resolvedListeners = Collections.emptyList();
            if (beanFactory instanceof ListableBeanFactory) {
                resolvedListeners = new ArrayList<>(
                    ((ListableBeanFactory) beanFactory).getBeansOfType(AutowireCandidateResolvingListener.class).values());
                AnnotationAwareOrderComparator.sort(resolvedListeners);
            }
            this.delegate = autowireCandidateResolver;
            addListeners(resolvedListeners);
            dbf.setAutowireCandidateResolver(this);
            logger.info("The ListenableAutowireCandidateResolver has been wrapped and registered to BeanFactory[{}]", dbf);
        }
    }
}
