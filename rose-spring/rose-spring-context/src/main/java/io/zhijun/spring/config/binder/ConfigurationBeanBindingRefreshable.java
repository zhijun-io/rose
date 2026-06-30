package io.zhijun.spring.config.binder;

import io.zhijun.spring.context.SpringContextHolder;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import io.zhijun.spring.config.property.Refreshable;

/**
 * {@link Refreshable} that rebinds {@link EnableConfigurationBeanBinding} beans when matching
 * configuration keys change.
 * <p>
 * Registered in {@code META-INF/spring.factories}. Invoked from
 * {@link io.zhijun.spring.config.property.PropertySourcesRefreshEnvironmentListener} after
 * {@code PropertySourcesChangedEvent#getChangedKeys()} is computed.
 * <p>
 * Rebind uses {@link ConfigurationBeanBindingPostProcessor#rebindConfigurationBean} on the
 * <em>existing</em> bean instance; bean definitions are not re-registered.
 */
public final class ConfigurationBeanBindingRefreshable implements Refreshable {

    @Override
    public boolean supports(Set<String> changedKeys) {
        ApplicationContext context = SpringContextHolder.peekRefreshableContext();
        if (context == null || changedKeys == null || changedKeys.isEmpty()) {
            return false;
        }
        ConfigurableListableBeanFactory beanFactory = getBeanFactory(context);
        if (beanFactory == null) {
            return false;
        }
        return !findAffectedConfigurationBeanNames(changedKeys, beanFactory).isEmpty();
    }

    @Override
    public void refresh(Set<String> changedKeys) {
        ApplicationContext context = SpringContextHolder.getRefreshableContext();
        if (!(context instanceof ConfigurableApplicationContext)) {
            return;
        }
        ConfigurableEnvironment environment = ((ConfigurableApplicationContext) context).getEnvironment();
        ConfigurableListableBeanFactory beanFactory = getBeanFactory(context);
        if (beanFactory == null || !beanFactory.containsBean(ConfigurationBeanBindingPostProcessor.BEAN_NAME)) {
            return;
        }
        ConfigurationBeanBindingPostProcessor processor = beanFactory.getBean(
                ConfigurationBeanBindingPostProcessor.BEAN_NAME, ConfigurationBeanBindingPostProcessor.class);
        for (String beanName : findAffectedConfigurationBeanNames(changedKeys, beanFactory)) {
            processor.rebindConfigurationBean(beanName, environment);
        }
    }

    private static List<String> findAffectedConfigurationBeanNames(
            Set<String> changedKeys, ConfigurableListableBeanFactory beanFactory) {
        List<String> affected = new ArrayList<String>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (!ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)) {
                continue;
            }
            String prefix = getPrefix(definition);
            if (ConfigurationBeanBindingSupport.prefixAffected(prefix, changedKeys)) {
                affected.add(beanName);
            }
        }
        return affected;
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
