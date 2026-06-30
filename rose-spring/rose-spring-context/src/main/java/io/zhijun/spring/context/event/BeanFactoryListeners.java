package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects and dispatches events to all registered {@link BeanFactoryListener} beans.
 * <p>
 * Use {@link #fireBeanDefinitionRegistryReady} / {@link #fireBeanFactoryReady} /
 * {@link #fireBeanFactoryConfigurationFrozen} to notify listeners.
 * <p>
 * (借鉴 microsphere-spring {@code BeanFactoryListeners})
 *
 * @see BeanFactoryListener
 * @see BeanFactoryListenerAdapter
 */
public class BeanFactoryListeners {

    private static final Logger logger = LoggerFactory.getLogger(BeanFactoryListeners.class);

    private final List<BeanFactoryListener> listeners;

    /**
     * Resolve all {@link BeanFactoryListener} beans from the given {@link BeanFactory}.
     */
    public BeanFactoryListeners(BeanFactory beanFactory) {
        List<BeanFactoryListener> resolved = new ArrayList<>();
        if (beanFactory instanceof ListableBeanFactory) {
            resolved.addAll(((ListableBeanFactory) beanFactory).getBeansOfType(BeanFactoryListener.class).values());
        }
        AnnotationAwareOrderComparator.sort(resolved);
        this.listeners = Collections.unmodifiableList(resolved);
    }

    public BeanFactoryListeners(List<BeanFactoryListener> listeners) {
        List<BeanFactoryListener> sorted = new ArrayList<>(listeners);
        AnnotationAwareOrderComparator.sort(sorted);
        this.listeners = Collections.unmodifiableList(sorted);
    }

    public List<BeanFactoryListener> getListeners() {
        return listeners;
    }

    public void fireBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
        for (BeanFactoryListener listener : listeners) {
            try {
                listener.onBeanDefinitionRegistryReady(registry);
            } catch (Exception ex) {
                logger.warn("BeanFactoryListener [{}] failed on onBeanDefinitionRegistryReady",
                        listener.getClass().getName(), ex);
            }
        }
    }

    public void fireBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
        for (BeanFactoryListener listener : listeners) {
            try {
                listener.onBeanFactoryReady(beanFactory);
            } catch (Exception ex) {
                logger.warn("BeanFactoryListener [{}] failed on onBeanFactoryReady",
                        listener.getClass().getName(), ex);
            }
        }
    }

    public void fireBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        for (BeanFactoryListener listener : listeners) {
            try {
                listener.onBeanFactoryConfigurationFrozen(beanFactory);
            } catch (Exception ex) {
                logger.warn("BeanFactoryListener [{}] failed on onBeanFactoryConfigurationFrozen",
                        listener.getClass().getName(), ex);
            }
        }
    }

}
