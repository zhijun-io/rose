package io.zhijun.spring.beans.factory.config;

import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Comparator;

/**
 * A comparator for {@link NamedBeanHolder} instances, typically used to sort
 * bean holders based on the order of their contained bean instances.
 *
 * <p>This comparator delegates the actual comparison logic to
 * {@link AnnotationAwareOrderComparator}, which takes into account
 * the {@link org.springframework.core.Ordered} interface as well as
 * the {@link java.util.Collections#reverseOrder()} if applicable.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * List<NamedBeanHolder<MyBean>> beanHolders = getBeanHolders();
 * beanHolders.sort(NamedBeanHolderComparator.INSTANCE);
 * }</pre>
 *
 * @param <T> the type of the bean instance held by the {@link NamedBeanHolder}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationAwareOrderComparator
 * @see NamedBeanHolder
 * @since 1.0.0
 */
public class NamedBeanHolderComparator<T> implements Comparator<NamedBeanHolder<T>> {

    public static final NamedBeanHolderComparator INSTANCE = new NamedBeanHolderComparator();

    /**
     * Compares two {@link NamedBeanHolder} instances by delegating to
     * {@link AnnotationAwareOrderComparator} using the held bean instances.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   NamedBeanHolder holder1 = new NamedBeanHolder("bean1", bean1);
     *   NamedBeanHolder holder2 = new NamedBeanHolder("bean2", bean2);
     *   int result = NamedBeanHolderComparator.INSTANCE.compare(holder1, holder2);
     * }</pre>
     *
     * @param o1 the first {@link NamedBeanHolder} to compare
     * @param o2 the second {@link NamedBeanHolder} to compare
     * @return a negative integer, zero, or a positive integer as the first
     * holder's bean instance is less than, equal to, or greater than the second
     */
    @Override
    public int compare(NamedBeanHolder<T> o1, NamedBeanHolder<T> o2) {
        return AnnotationAwareOrderComparator.INSTANCE.compare(o1.getBeanInstance(), o2.getBeanInstance());
    }
}
