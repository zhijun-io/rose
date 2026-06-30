package io.zhijun.spring.beans.factory.support;

import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

public class CompositeAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private final List<AutowireCandidateResolvingListener> listeners = new LinkedList<>();

    public CompositeAutowireCandidateResolvingListener(List<AutowireCandidateResolvingListener> listeners) {
        Assert.notEmpty(listeners, "The argument 'listeners' must not be empty!");
        this.addListeners(listeners);
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        listeners.forEach(listener -> {
            Assert.notNull(listener, "The element 'listener' must not be null!");
            this.listeners.add(listener);
        });
        AnnotationAwareOrderComparator.sort(this.listeners);
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        listeners.forEach(listener -> listener.suggestedValueResolved(descriptor, suggestedValue));
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        listeners.forEach(listener -> listener.lazyProxyResolved(descriptor, beanName, proxy));
    }
}
