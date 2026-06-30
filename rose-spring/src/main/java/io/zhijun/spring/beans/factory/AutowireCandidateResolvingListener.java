package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface AutowireCandidateResolvingListener {

    static List<AutowireCandidateResolvingListener> loadListeners(@Nullable BeanFactory beanFactory) {
        List<AutowireCandidateResolvingListener> listeners = new ArrayList<>();
        if (beanFactory instanceof ListableBeanFactory) {
            listeners.addAll(((ListableBeanFactory) beanFactory).getBeansOfType(AutowireCandidateResolvingListener.class).values());
        }
        AnnotationAwareOrderComparator.sort(listeners);
        return Collections.unmodifiableList(listeners);
    }

    default void suggestedValueResolved(DependencyDescriptor descriptor, @Nullable Object suggestedValue) {
    }

    default void lazyProxyResolved(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Object proxy) {
    }
}
