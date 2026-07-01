 package io.zhijun.spring.beans.factory.support;

 import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.List;

/**
 * 复合 {@link AutowireCandidateResolvingListener}，将事件委托给内部监听器列表。
 */
public class CompositeAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private final List<AutowireCandidateResolvingListener> listeners;

    public CompositeAutowireCandidateResolvingListener(List<AutowireCandidateResolvingListener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            throw new IllegalArgumentException("The argument 'listeners' must not be empty!");
        }
        this.listeners = listeners;
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        for (AutowireCandidateResolvingListener listener : listeners) {
            listener.suggestedValueResolved(descriptor, suggestedValue);
        }
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        for (AutowireCandidateResolvingListener listener : listeners) {
            listener.lazyProxyResolved(descriptor, beanName, proxy);
        }
    }
}
