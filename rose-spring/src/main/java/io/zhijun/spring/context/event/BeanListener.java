package io.zhijun.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * Listens for per-bean lifecycle events. Register as a Spring bean to receive
 * notifications at each stage of the bean lifecycle.
 * <p>
 * (借鉴 microsphere-spring {@code BeanListener})
 *
 * @see BeanListeners
 */
public interface BeanListener extends EventListener {

    boolean supports(String beanName);

    default void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Constructor<?> constructor, Object[] args) {
    }

    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Object factoryBean, Method factoryMethod, Object[] args) {
    }

    default void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
    }

    default void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    default void onBeforeBeanInitialize(String beanName, Object bean) {
    }

    default void onAfterBeanInitialized(String beanName, Object bean) {
    }

    default void onBeanReady(String beanName, Object bean) {
    }

    default void onBeforeBeanDestroy(String beanName, Object bean) {
    }

    default void onAfterBeanDestroy(String beanName, Object bean) {
    }
}
