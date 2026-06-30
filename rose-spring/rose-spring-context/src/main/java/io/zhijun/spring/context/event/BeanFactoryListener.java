package io.zhijun.spring.context.event;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.EventListener;

/**
 * Listens for key lifecycle events of a Spring {@link BeanFactory}.
 * <p>
 * Implementations receive notifications when the {@link BeanDefinitionRegistry} is ready,
 * when the {@link ConfigurableListableBeanFactory} is fully configured, and when the
 * bean factory configuration is frozen.
 * <p>
 * (借鉴 microsphere-spring {@code BeanFactoryListener})
 *
 * @see BeanFactoryListenerAdapter
 * @see BeanFactoryListeners
 * @see ConfigurableListableBeanFactory
 */
public interface BeanFactoryListener extends EventListener {

    /**
     * Called when the {@link BeanDefinitionRegistry} is ready.
     */
    default void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
    }

    /**
     * Called when the {@link ConfigurableListableBeanFactory} is ready.
     */
    default void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
    }

    /**
     * Called when the bean factory configuration is frozen.
     */
    default void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
    }

}
