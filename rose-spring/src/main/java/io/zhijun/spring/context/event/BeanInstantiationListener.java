package io.zhijun.spring.context.event;

import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * Listens for bean instantiation lifecycle events: bean definition ready,
 * before/after instantiation via constructor or factory method.
 * <p>
 * Implement this interface instead of {@link BeanListener} when you only
 * need to observe the instantiation phase. If you need multiple phases,
 * implement {@link BeanListener} instead — it combines all sub-interfaces.
 *
 * @see BeanListener
 * @see BeanInitializationListener
 * @see BeanDestructionListener
 */
public interface BeanInstantiationListener extends EventListener {

    /**
     * Called when the merged bean definition is ready.
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged bean definition
     */
    default void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    /**
     * Called before a bean is instantiated via its no-arg constructor.
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged bean definition
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    /**
     * Called before a bean is instantiated via a specific constructor.
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged bean definition
     * @param constructor          the constructor to use
     * @param args                 the constructor arguments
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Constructor<?> constructor, Object[] args) {
    }

    /**
     * Called before a bean is instantiated via a factory method.
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged bean definition
     * @param factoryBean          the factory bean (may be null for static methods)
     * @param factoryMethod        the factory method
     * @param args                 the factory method arguments
     */
    default void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                         Object factoryBean, Method factoryMethod, Object[] args) {
    }

    /**
     * Called after a bean has been instantiated.
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged bean definition
     * @param bean                 the instantiated bean (may be null if instantiation failed)
     */
    default void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
    }
}
