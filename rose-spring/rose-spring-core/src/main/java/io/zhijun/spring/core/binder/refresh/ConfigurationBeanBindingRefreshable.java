package io.zhijun.spring.core.binder.refresh;

import java.util.Set;

import io.zhijun.spring.core.binder.annotation.ConfigurationBeanBindingPostProcessor;
import io.zhijun.spring.core.binder.support.ConfigurationBeanBindingSupport;
import io.zhijun.spring.core.env.refresh.Refreshable;
import io.zhijun.spring.core.env.refresh.RefreshableContextHolder;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Rebinds {@link io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding} beans when
 * matching configuration keys change.
 */
public final class ConfigurationBeanBindingRefreshable implements Refreshable {

    @Override
    public boolean supports(Set<String> changedKeys) {
        ApplicationContext context = RefreshableContextHolder.peekApplicationContext();
        if (context == null || changedKeys == null || changedKeys.isEmpty()) {
            return false;
        }
        ConfigurableListableBeanFactory beanFactory = getBeanFactory(context);
        if (beanFactory == null) {
            return false;
        }
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (!ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)) {
                continue;
            }
            String prefix = getPrefix(definition);
            if (ConfigurationBeanBindingSupport.prefixAffected(prefix, changedKeys)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void refresh(Set<String> changedKeys) {
        ApplicationContext context = RefreshableContextHolder.getApplicationContext();
        if (!(context instanceof ConfigurableApplicationContext)) {
            return;
        }
        ConfigurableEnvironment environment = ((ConfigurableApplicationContext) context).getEnvironment();
        ConfigurableListableBeanFactory beanFactory = getBeanFactory(context);
        if (beanFactory == null || !beanFactory.containsBean(ConfigurationBeanBindingPostProcessor.BEAN_NAME)) {
            return;
        }
        ConfigurationBeanBindingPostProcessor processor = beanFactory
                .getBean(ConfigurationBeanBindingPostProcessor.BEAN_NAME, ConfigurationBeanBindingPostProcessor.class);
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (!ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)) {
                continue;
            }
            String prefix = getPrefix(definition);
            if (!ConfigurationBeanBindingSupport.prefixAffected(prefix, changedKeys)) {
                continue;
            }
            processor.rebindConfigurationBean(beanName, environment);
        }
    }

    private static ConfigurableListableBeanFactory getBeanFactory(ApplicationContext context) {
        if (context instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) context).getBeanFactory();
        }
        return null;
    }

    private static String getPrefix(BeanDefinition definition) {
        return (String) definition.getAttribute(ConfigurationBeanBindingSupport.CONFIGURATION_BINDING_PREFIX);
    }
}
