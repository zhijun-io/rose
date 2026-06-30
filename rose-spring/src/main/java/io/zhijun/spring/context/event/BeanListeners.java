package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
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
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Composite {@link BeanListener} that collects all {@link BeanListener} beans from the
 * {@link ConfigurableListableBeanFactory} and dispatches events to each.
 * <p>
 * (借鉴 microsphere-spring {@code BeanListeners})
 *
 * @see BeanListener
 */
public class BeanListeners implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<NamedBeanHolder<BeanListener>> namedListeners;

    private final Set<String> readyBeanNames;

    public BeanListeners(ConfigurableListableBeanFactory beanFactory) {
        this.namedListeners = resolveListeners(beanFactory);
        this.readyBeanNames = getReadyBeanNames(beanFactory);
    }

    private static List<NamedBeanHolder<BeanListener>> resolveListeners(ConfigurableListableBeanFactory beanFactory) {
        List<NamedBeanHolder<BeanListener>> holders = new ArrayList<>();
        for (String name : beanFactory.getBeanNamesForType(BeanListener.class)) {
            holders.add(new NamedBeanHolder<>(name, beanFactory.getBean(name, BeanListener.class)));
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

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(beanName, l -> l.onBeanDefinitionReady(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Constructor<?> constructor, Object[] args) {
        iterate(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, constructor, args));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition,
                                        Object factoryBean, Method factoryMethod, Object[] args) {
        iterate(beanName, l -> l.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, factoryBean, factoryMethod, args));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        iterate(beanName, l -> l.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean));
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        iterate(beanName, l -> l.onBeanPropertyValuesReady(beanName, bean, pvs));
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        iterate(beanName, l -> l.onBeforeBeanInitialize(beanName, bean));
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        iterate(beanName, l -> l.onAfterBeanInitialized(beanName, bean));
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        iterate(beanName, l -> l.onBeanReady(beanName, bean));
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        iterate(beanName, l -> l.onBeforeBeanDestroy(beanName, bean));
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        iterate(beanName, l -> l.onAfterBeanDestroy(beanName, bean));
    }

    private void iterate(String beanName, Consumer<BeanListener> action) {
        if (isIgnored(beanName)) {
            return;
        }
        for (NamedBeanHolder<BeanListener> holder : namedListeners) {
            BeanListener listener = holder.getBeanInstance();
            try {
                if (listener.supports(beanName)) {
                    action.accept(listener);
                }
            } catch (Exception ex) {
                logger.warn("BeanListener [{}] failed for bean [{}]",
                        holder.getBeanName(), beanName, ex);
            }
        }
    }
}
