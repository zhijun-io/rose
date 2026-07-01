package io.zhijun.spring.beans.factory.support;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.zhijun.spring.constants.PropertyConstants.LISTENABLE_AUTOWIRE_CANDIDATE_RESOLVER_PROPERTY_NAME_PREFIX;

/**
 * {@link ConfigurableApplicationContextInitializer} 实现，用于注册 {@link ListenableAutowireCandidateResolver}。
 * 配置属性：{@code rose.spring.listenable-autowire-candidate-resolver.enabled}（默认 false）。
 */
public class ListenableAutowireCandidateResolverInitializer extends ConfigurableApplicationContextInitializer {

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        BeanRegistrar.registerBeanDefinition((BeanDefinitionRegistry) beanFactory, null, ListenableAutowireCandidateResolver.class);
    }

    @Override
    public String getEnabledPropertyName() {
        return LISTENABLE_AUTOWIRE_CANDIDATE_RESOLVER_PROPERTY_NAME_PREFIX;
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
