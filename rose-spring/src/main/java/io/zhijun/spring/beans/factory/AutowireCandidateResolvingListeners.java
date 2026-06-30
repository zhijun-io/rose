package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import io.zhijun.core.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for discovering {@link AutowireCandidateResolvingListener} beans from a Spring {@link BeanFactory}.
 *
 * @see AutowireCandidateResolvingListener
 */
public abstract class AutowireCandidateResolvingListeners {

    public static List<AutowireCandidateResolvingListener> loadListeners(@Nullable BeanFactory beanFactory) {
        List<AutowireCandidateResolvingListener> listeners = new ArrayList<>();
        if (beanFactory instanceof ListableBeanFactory) {
            listeners.addAll(((ListableBeanFactory) beanFactory).getBeansOfType(AutowireCandidateResolvingListener.class).values());
        }
        AnnotationAwareOrderComparator.sort(listeners);
        return Collections.unmodifiableList(listeners);
    }

    private AutowireCandidateResolvingListeners() {
    }
}
