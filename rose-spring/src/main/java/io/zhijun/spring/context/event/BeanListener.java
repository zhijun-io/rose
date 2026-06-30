package io.zhijun.spring.context.event;

import java.util.EventListener;

/**
 * Composite listener for all bean lifecycle events.
 * <p>
 * Combines {@link BeanInstantiationListener}, {@link BeanInitializationListener},
 * and {@link BeanDestructionListener} into a single interface. Implement this
 * when you need to observe multiple lifecycle phases; implement the specific
 * sub-interface when you only need one phase.
 * <p>
 * (借鉴 microsphere-spring {@code BeanListener})
 *
 * @see BeanListeners
 * @see BeanInstantiationListener
 * @see BeanInitializationListener
 * @see BeanDestructionListener
 */
public interface BeanListener extends BeanInstantiationListener,
        BeanInitializationListener, BeanDestructionListener {

    /**
     * Check whether this listener supports the given bean name.
     * <p>
     * Only applies when set directly on this interface; sub-interface-only
     * implementers receive all events without filtering.
     *
     * @param beanName the bean name
     * @return true if the bean should be processed
     */
    boolean supports(String beanName);
}
