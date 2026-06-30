package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.InstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean Before-Event Publishing Processor — 装饰 InstantiationStrategy 拦截 bean 创建，
 * 向 {@link BeanListener} 派发前置生命周期事件。
 * <p>
 * 同时作为 BFPP 重新注册所有 bean definition，确保
 * {@link EventPublishingBeanAfterProcessor.Initializer} 为第一个处理的 bean。
 * <p>
 * （借鉴 microsphere-spring {@code EventPublishingBeanBeforeProcessor}）
 *
 * @see EventPublishingBeanAfterProcessor
 * @see BeanListeners
 * @see BeanListener
 * @see EventPublishingBeanInitializer
 */
public class EventPublishingBeanBeforeProcessor
        implements InstantiationAwareBeanPostProcessor, BeanDefinitionRegistryPostProcessor,
        DestructionAwareBeanPostProcessor, InstantiationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingBeanBeforeProcessor.class);

    private static final Method GET_INSTANTIATION_STRATEGY_METHOD =
            ReflectionUtils.findMethod(AbstractAutowireCapableBeanFactory.class, "getInstantiationStrategy");

    private BeanDefinitionRegistry registry;

    private InstantiationStrategy instantiationStrategyDelegate;

    private BeanListeners beanEventListeners;

    private BeanFactoryListeners beanFactoryListeners;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
        registerBeanFactoryListeners(registry);
        prepareBeanDefinitions(registry);
        fireBeanDefinitionRegistryReadyEvent(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        fireBeanFactoryReadyEvent(beanFactory);
        registerBeanEventListeners(beanFactory);
        decorateInstantiationStrategy(beanFactory);
        beanFactory.addBeanPostProcessor(this);
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner) throws BeansException {
        this.beanEventListeners.onBeforeBeanInstantiate(beanName, bd);
        Object bean = null;
        try {
            bean = instantiationStrategyDelegate.instantiate(bd, beanName, owner);
        } finally {
            this.beanEventListeners.onAfterBeanInstantiated(beanName, bd, bean);
        }
        return bean;
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
                              Constructor<?> ctor, Object... args) throws BeansException {
        this.beanEventListeners.onBeforeBeanInstantiate(beanName, bd, ctor, args);
        Object bean = null;
        try {
            bean = instantiationStrategyDelegate.instantiate(bd, beanName, owner, ctor, args);
        } finally {
            this.beanEventListeners.onAfterBeanInstantiated(beanName, bd, bean);
        }
        return bean;
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
                              Object factoryBean, Method factoryMethod, Object... args) throws BeansException {
        this.beanEventListeners.onBeforeBeanInstantiate(beanName, bd, factoryBean, factoryMethod, args);
        Object bean = null;
        try {
            bean = instantiationStrategyDelegate.instantiate(bd, beanName, owner, factoryBean, factoryMethod, args);
        } finally {
            this.beanEventListeners.onAfterBeanInstantiated(beanName, bd, bean);
        }
        return bean;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        this.beanEventListeners.onBeanPropertyValuesReady(beanName, bean, pvs);
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onBeforeBeanInitialize(beanName, bean);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onBeforeBeanDestroy(beanName, bean);
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    private void prepareBeanDefinitions(BeanDefinitionRegistry registry) {
        String[] beanNames = registry.getBeanDefinitionNames();
        List<BeanDefinitionHolder> holders = new ArrayList<>(beanNames.length);
        for (String beanName : beanNames) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            holders.add(new BeanDefinitionHolder(bd, beanName));
            registry.removeBeanDefinition(beanName);
        }

        // 注册 EventPublishingBeanAfterProcessor.Initializer 确保其为第一个 bean definition
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .rootBeanDefinition(EventPublishingBeanAfterProcessor.Initializer.class);
        registry.registerBeanDefinition(
                "eventPublishingBeanAfterProcessorInitializer",
                builder.getBeanDefinition());

        // 按原来顺序重新注册
        for (BeanDefinitionHolder holder : holders) {
            org.springframework.beans.factory.support.BeanDefinitionReaderUtils
                    .registerBeanDefinition(holder, registry);
        }
    }

    private void fireBeanDefinitionRegistryReadyEvent(BeanDefinitionRegistry registry) {
        beanFactoryListeners.fireBeanDefinitionRegistryReady(registry);
    }

    private void fireBeanFactoryReadyEvent(ConfigurableListableBeanFactory beanFactory) {
        beanFactoryListeners.fireBeanFactoryReady(beanFactory);
    }

    private void registerBeanFactoryListeners(BeanDefinitionRegistry registry) {
        if (registry instanceof ConfigurableListableBeanFactory) {
            BeanFactoryListeners listeners = new BeanFactoryListeners((ConfigurableListableBeanFactory) registry);
            listeners.registerBean(registry);
            this.beanFactoryListeners = listeners;
        }
    }

    private void registerBeanEventListeners(ConfigurableListableBeanFactory beanFactory) {
        BeanListeners listeners = new BeanListeners(beanFactory);
        listeners.registerBean(registry);
        this.beanEventListeners = listeners;
    }

    private void decorateInstantiationStrategy(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory autowireFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
            this.instantiationStrategyDelegate = getDelegate(autowireFactory);
            if (instantiationStrategyDelegate != this) {
                autowireFactory.setInstantiationStrategy(this);
            }
        }
    }

    private InstantiationStrategy getDelegate(AbstractAutowireCapableBeanFactory beanFactory) {
        return (InstantiationStrategy) ReflectionUtils.invokeMethod(GET_INSTANTIATION_STRATEGY_METHOD, beanFactory);
    }
}
