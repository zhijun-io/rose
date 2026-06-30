package io.zhijun.spring.context.event;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Abstract adapter for {@link BeanFactoryListener}. Override only the methods you need.
 *
 * @see BeanFactoryListener
 */
public abstract class BeanFactoryListenerAdapter implements BeanFactoryListener {

    @Override
    public void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
    }

    @Override
    public void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
    }

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
    }

}
