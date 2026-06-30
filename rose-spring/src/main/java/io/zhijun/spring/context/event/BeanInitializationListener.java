package io.zhijun.spring.context.event;

import org.springframework.beans.PropertyValues;

import java.util.EventListener;

/**
 * Listens for bean initialization lifecycle events: property values ready,
 * before/after initialization, and bean fully ready.
 * <p>
 * Implement this interface instead of {@link BeanListener} when you only
 * need to observe the initialization phase. If you need multiple phases,
 * implement {@link BeanListener} instead — it combines all sub-interfaces.
 *
 * @see BeanListener
 * @see BeanInstantiationListener
 * @see BeanDestructionListener
 */
public interface BeanInitializationListener extends EventListener {

    /**
     * Called when property values for a bean are ready.
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     * @param pvs      the property values
     */
    default void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    /**
     * Called before a bean's init method is invoked.
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     */
    default void onBeforeBeanInitialize(String beanName, Object bean) {
    }

    /**
     * Called after a bean's init method has completed.
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     */
    default void onAfterBeanInitialized(String beanName, Object bean) {
    }

    /**
     * Called when a bean is fully ready (initialized and all post-processors applied).
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     */
    default void onBeanReady(String beanName, Object bean) {
    }
}
