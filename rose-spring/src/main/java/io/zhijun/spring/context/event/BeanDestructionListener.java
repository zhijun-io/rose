package io.zhijun.spring.context.event;

import java.util.EventListener;

/**
 * Listens for bean destruction lifecycle events: before/after destroy.
 * <p>
 * Implement this interface instead of {@link BeanListener} when you only
 * need to observe the destruction phase. If you need multiple phases,
 * implement {@link BeanListener} instead — it combines all sub-interfaces.
 *
 * @see BeanListener
 * @see BeanInstantiationListener
 * @see BeanInitializationListener
 */
public interface BeanDestructionListener extends EventListener {

    /**
     * Called before a bean is destroyed.
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     */
    default void onBeforeBeanDestroy(String beanName, Object bean) {
    }

    /**
     * Called after a bean has been destroyed.
     *
     * @param beanName the bean name
     * @param bean     the bean instance
     */
    default void onAfterBeanDestroy(String beanName, Object bean) {
    }
}
