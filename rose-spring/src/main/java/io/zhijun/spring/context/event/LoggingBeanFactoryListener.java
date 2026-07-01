package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 日志实现的 {@link BeanFactoryListener}。
 */
public class LoggingBeanFactoryListener implements BeanFactoryListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBeanFactoryListener.class);

    @Override
    public void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanDefinitionRegistryReady - BeanDefinitionRegistry : {}", registry);
        }
    }

    @Override
    public void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanFactoryReady - BeanFactory : {}", beanFactory);
        }
    }

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        if (logger.isInfoEnabled()) {
            logger.info("onBeanFactoryConfigurationFrozen - BeanFactory : {}", beanFactory);
        }
    }
}
