package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import org.springframework.beans.factory.config.DependencyDescriptor;

public interface AutowireCandidateResolvingListener {

    default void suggestedValueResolved(DependencyDescriptor descriptor, @Nullable Object suggestedValue) {
    }

    default void lazyProxyResolved(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Object proxy) {
    }
}
