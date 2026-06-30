package io.zhijun.spring.beans.factory.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import io.zhijun.core.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static io.zhijun.spring.beans.factory.support.AutowireCandidateResolvingListener.loadListeners;

public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor,
        BeanNameAware {

    private static final Logger logger = LoggerFactory.getLogger(ListenableAutowireCandidateResolver.class);

    private AutowireCandidateResolver delegate;

    private CompositeAutowireCandidateResolvingListener compositeListener;

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
        compositeListener.addListeners(listeners);
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
        compositeListener.suggestedValueResolved(descriptor, suggestedValue);
        return suggestedValue;
    }

    @Nullable
    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Object proxy = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        compositeListener.lazyProxyResolved(descriptor, beanName, proxy);
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
            List<AutowireCandidateResolvingListener> listeners = loadListeners(beanFactory);
            CompositeAutowireCandidateResolvingListener compositeListener = new CompositeAutowireCandidateResolvingListener(listeners);
            this.delegate = autowireCandidateResolver;
            this.compositeListener = compositeListener;
            dbf.setAutowireCandidateResolver(this);
            logger.info("The ListenableAutowireCandidateResolver has been wrapped and registered to BeanFactory[{}]", dbf);
        }
    }
}
