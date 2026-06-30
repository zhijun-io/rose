package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 提供日志能力的 {@link BeanFactoryListener} 实现，记录 BeanDefinitionRegistry 就绪、
 * BeanFactory 就绪、配置冻结等关键生命周期事件。
 * <p>
 * （移植自 microsphere-spring {@code LoggingBeanFactoryListener}）
 *
 * @see BeanFactoryListener
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
