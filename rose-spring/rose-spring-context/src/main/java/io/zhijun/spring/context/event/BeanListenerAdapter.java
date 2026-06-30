package io.zhijun.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Convenience adapter for {@link BeanListener}. All methods have empty defaults
 * and {@link #supports(String)} returns {@code true} by default.
 * <p>
 * Override only the methods you need.
 *
 * @see BeanListener
 */
public interface BeanListenerAdapter extends BeanListener {

    @Override
    default boolean supports(String beanName) {
        return true;
    }

    @Override
    default void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    @Override
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    @Override
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Constructor<?> constructor, Object[] args) {
    }

    @Override
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Object factoryBean, Method factoryMethod, Object[] args) {
    }

    @Override
    default void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
    }

    @Override
    default void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    @Override
    default void onBeforeBeanInitialize(String beanName, Object bean) {
    }

    @Override
    default void onAfterBeanInitialized(String beanName, Object bean) {
    }

    @Override
    default void onBeanReady(String beanName, Object bean) {
    }

    @Override
    default void onBeforeBeanDestroy(String beanName, Object bean) {
    }

    @Override
    default void onAfterBeanDestroy(String beanName, Object bean) {
    }
}
