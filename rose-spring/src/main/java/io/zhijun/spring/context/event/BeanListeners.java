package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Composite {@link BeanListener} that collects all beans implementing
 * {@link BeanInstantiationListener}, {@link BeanInitializationListener}, and
 * {@link BeanDestructionListener} from the {@link ConfigurableListableBeanFactory}
 * and dispatches events to each.
 * <p>
 * Registration as a Spring bean via {@link #registerBean(BeanDefinitionRegistry)}.
 * <p>
 * (借鉴 microsphere-spring {@code BeanListeners})
 *
 * @see BeanListener
 * @see BeanInstantiationListener
 * @see BeanInitializationListener
 * @see BeanDestructionListener
 */
public class BeanListeners implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    /**
     * Beans that implement {@link BeanInstantiationListener} (including {@link BeanListener} implementers).
     */
    private final List<NamedBeanHolder<BeanInstantiationListener>> instantiationListeners;

    /**
     * Beans that implement {@link BeanInitializationListener} (including {@link BeanListener} implementers).
     */
    private final List<NamedBeanHolder<BeanInitializationListener>> initializationListeners;

    /**
     * Beans that implement {@link BeanDestructionListener} (including {@link BeanListener} implementers).
     */
    private final List<NamedBeanHolder<BeanDestructionListener>> destructionListeners;

    private final Set<String> readyBeanNames;

    public BeanListeners(ConfigurableListableBeanFactory beanFactory) {
        this.readyBeanNames = getReadyBeanNames(beanFactory);

        // Resolve beans by sub-interface, deduplicating by bean name.
        // BeanListener implementers span all 3 phases, so they go into every list.
        Set<String> seen = new HashSet<>();
        this.instantiationListeners = resolveBeanListeners(beanFactory, BeanInstantiationListener.class, seen);
        this.initializationListeners = resolveBeanListeners(beanFactory, BeanInitializationListener.class, seen);
        this.destructionListeners = resolveBeanListeners(beanFactory, BeanDestructionListener.class, seen);
    }

    /**
     * Resolve all beans of the given listener type, skipping those already seen,
     * and sort by {@link org.springframework.core.annotation.Order @Order} /
     * {@link org.springframework.core.Ordered Ordered}.
     */
    private static <T> List<NamedBeanHolder<T>> resolveBeanListeners(
            ConfigurableListableBeanFactory beanFactory, Class<T> listenerType, Set<String> seen) {
        List<NamedBeanHolder<T>> holders = new ArrayList<>();
        for (String name : beanFactory.getBeanNamesForType(listenerType)) {
            if (!seen.contains(name)) {
                holders.add(new NamedBeanHolder<>(name, beanFactory.getBean(name, listenerType)));
                seen.add(name);
            }
        }
        AnnotationAwareOrderComparator.sort(holders);
        return holders;
    }

    static Set<String> getReadyBeanNames(ConfigurableListableBeanFactory beanFactory) {
        String[] singletonNames = beanFactory.getSingletonNames();
        return new java.util.LinkedHashSet<>(Arrays.asList(singletonNames));
    }

    void setReadyBeanNames(Set<String> readyBeanNames) {
        this.readyBeanNames.clear();
        this.readyBeanNames.addAll(readyBeanNames);
    }

    void registerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(BeanListeners.class, () -> this);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(BEAN_NAME, builder.getBeanDefinition());
    }

    static BeanListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanListeners.class);
    }

    private boolean isIgnored(String beanName) {
        return this.readyBeanNames.contains(beanName) || BEAN_NAME.equals(beanName);
    }

    @Override
    public boolean supports(String beanName) {
        return true;
    }

    // ---- Instantiation phase dispatch ----

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        dispatch(instantiationListeners, beanName, l -> l.onBeanDefinitionReady(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        dispatch(instantiationListeners, beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Constructor<?> constructor, Object[] args) {
        dispatch(instantiationListeners, beanName,
                l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, constructor, args));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Object factoryBean, Method factoryMethod, Object[] args) {
        dispatch(instantiationListeners, beanName,
                l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, factoryBean, factoryMethod, args));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        dispatch(instantiationListeners, beanName,
                l -> l.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean));
    }

    // ---- Initialization phase dispatch ----

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        dispatch(initializationListeners, beanName, l -> l.onBeanPropertyValuesReady(beanName, bean, pvs));
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        dispatch(initializationListeners, beanName, l -> l.onBeforeBeanInitialize(beanName, bean));
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        dispatch(initializationListeners, beanName, l -> l.onAfterBeanInitialized(beanName, bean));
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        dispatch(initializationListeners, beanName, l -> l.onBeanReady(beanName, bean));
    }

    // ---- Destruction phase dispatch ----

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        dispatch(destructionListeners, beanName, l -> l.onBeforeBeanDestroy(beanName, bean));
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        dispatch(destructionListeners, beanName, l -> l.onAfterBeanDestroy(beanName, bean));
    }

    /**
     * Dispatch to all listeners in the given list, applying the {@code supports()}
     * guard for {@link BeanListener} implementers.
     */
    private <T> void dispatch(List<NamedBeanHolder<T>> listeners, String beanName, Consumer<T> action) {
        if (isIgnored(beanName)) {
            return;
        }
        for (NamedBeanHolder<T> holder : listeners) {
            T listener = holder.getBeanInstance();
            // Only BeanListener implementers get supports() filtering;
            // sub-interface-only implementers receive all events.
            if (listener instanceof BeanListener && !((BeanListener) listener).supports(beanName)) {
                continue;
            }
            try {
                action.accept(listener);
            } catch (Exception ex) {
                logger.warn("BeanListener [{}] failed for bean [{}]",
                        holder.getBeanName(), beanName, ex);
            }
        }
    }
}
