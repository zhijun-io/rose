package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

import io.zhijun.core.annotation.Nullable;

public interface AutowireCandidateResolvingListener {

    default void suggestedValueResolved(DependencyDescriptor descriptor, @Nullable Object suggestedValue) {
    }

    default void lazyProxyResolved(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Object proxy) {
    }
}
