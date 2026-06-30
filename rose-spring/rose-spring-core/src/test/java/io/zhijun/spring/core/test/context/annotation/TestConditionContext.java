package io.zhijun.spring.core.test.context.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * 用于测试的 {@link ConditionContext} 实现。
 *
 * <p>与 {@link AnnotatedTypeMetadataTestFactory} 配合使用，
 * 可在非 Spring 容器环境中测试 {@link Condition} 实现。</p>
 */
public class TestConditionContext implements ConditionContext, ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return (BeanDefinitionRegistry) applicationContext.getBeanFactory();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return applicationContext.getBeanFactory();
    }

    @Override
    public Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return applicationContext;
    }

    @Override
    public ClassLoader getClassLoader() {
        return applicationContext.getClassLoader();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
