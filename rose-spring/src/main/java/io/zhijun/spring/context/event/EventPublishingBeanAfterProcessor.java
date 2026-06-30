package io.zhijun.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Bean After-Event Publishing Processor — 在 context refresh 后触发 {@link BeanListener#onBeanReady}，
 * 在 context close 时代理 {@link DisposableBean#destroy()} 触发 {@link BeanListener#onAfterBeanDestroy}。
 * <p>
 * （借鉴 microsphere-spring {@code EventPublishingBeanAfterProcessor}）
 *
 * @see EventPublishingBeanBeforeProcessor
 * @see BeanListeners
 * @see BeanListener
 */
public class EventPublishingBeanAfterProcessor
        implements InstantiationAwareBeanPostProcessor, GenericApplicationListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingBeanAfterProcessor.class);

    private static final Class<?> DISPOSABLE_BEAN_ADAPTER_CLASS;

    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("org.springframework.beans.factory.support.DisposableBeanAdapter");
        } catch (ClassNotFoundException e) {
            // should not happen in Spring 5.3+
        }
        DISPOSABLE_BEAN_ADAPTER_CLASS = clazz;
    }

    private final ConfigurableApplicationContext context;

    private final BeanListeners beanEventListeners;

    public EventPublishingBeanAfterProcessor(ConfigurableApplicationContext context) {
        this.context = context;
        this.beanEventListeners = BeanListeners.getBean(context.getBeanFactory());
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onAfterBeanInitialized(beanName, bean);
        return bean;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType)
                || ContextClosedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return eventType.isAssignableFrom(ContextRefreshedEvent.class)
                || eventType.isAssignableFrom(ContextClosedEvent.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!context.equals(event.getSource())) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent();
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent();
        }
    }

    private void onContextRefreshedEvent() {
        logger.debug("Context refreshed - firing onBeanReady for all beans");
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            if (beanFactory.containsSingleton(beanName)) {
                Object bean = beanFactory.getSingleton(beanName);
                this.beanEventListeners.onBeanReady(beanName, bean);
            }
        }
    }

    private void onContextClosedEvent() {
        decorateDisposableBeans();
    }

    @SuppressWarnings("unchecked")
    private void decorateDisposableBeans() {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (!(beanFactory instanceof DefaultSingletonBeanRegistry)) {
            return;
        }
        DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) beanFactory;

        ReflectionUtils.doWithFields(DefaultSingletonBeanRegistry.class,
                field -> {
                    field.setAccessible(true);
                    Map<String, Object> disposableBeans = (Map<String, Object>) field.get(registry);
                    if (disposableBeans == null) {
                        return;
                    }
                    for (Map.Entry<String, Object> entry : disposableBeans.entrySet()) {
                        String beanName = entry.getKey();
                        Object adapterBean = entry.getValue();
                        if (isDisposableBeanAdapter(adapterBean)) {
                            DisposableBean delegate = (DisposableBean) adapterBean;
                            DecoratingDisposableBean decorating =
                                    new DecoratingDisposableBean(beanName, delegate,
                                            this.beanEventListeners::onAfterBeanDestroy);
                            entry.setValue(decorating);
                        }
                    }
                },
                field -> "disposableBeans".equals(field.getName()) && Map.class.isAssignableFrom(field.getType()));
    }

    private boolean isDisposableBeanAdapter(Object bean) {
        return DISPOSABLE_BEAN_ADAPTER_CLASS != null
                && DISPOSABLE_BEAN_ADAPTER_CLASS.equals(bean.getClass())
                && bean instanceof DisposableBean;
    }

    /**
     * {@link EventPublishingBeanBeforeProcessor} 注册此 Initializer，
     * 其构造器负责创建并注册 {@link EventPublishingBeanAfterProcessor}。
     */
    static class Initializer {

        Initializer(ConfigurableApplicationContext context) {
            logger.debug("Initializing EventPublishingBeanAfterProcessor");
            EventPublishingBeanAfterProcessor processor = new EventPublishingBeanAfterProcessor(context);
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            beanFactory.addBeanPostProcessor(processor);
            context.addApplicationListener(processor);
            fireBeanFactoryConfigurationFrozenEvent(beanFactory);
            fireBeanDefinitionReadyEvent(beanFactory);
        }

        private void fireBeanFactoryConfigurationFrozenEvent(ConfigurableListableBeanFactory beanFactory) {
            BeanFactoryListeners listeners = BeanFactoryListeners.getBean(beanFactory);
            listeners.fireBeanFactoryConfigurationFrozen(beanFactory);
        }

        private void fireBeanDefinitionReadyEvent(ConfigurableListableBeanFactory beanFactory) {
            BeanListeners beanEventListeners = BeanListeners.getBean(beanFactory);
            Set<String> readyBeanNames = BeanListeners.getReadyBeanNames(beanFactory);
            beanEventListeners.setReadyBeanNames(readyBeanNames);

            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
                if (beanDefinition instanceof RootBeanDefinition) {
                    beanEventListeners.onBeanDefinitionReady(beanName, (RootBeanDefinition) beanDefinition);
                }
            }
        }
    }

    /**
     * 装饰 {@link DisposableBean}，在销毁后回调 {@link BeanListener#onAfterBeanDestroy}。
     */
    private static class DecoratingDisposableBean implements DisposableBean {

        private static final java.lang.reflect.Field BEAN_FIELD;

        static {
            java.lang.reflect.Field field = null;
            if (DISPOSABLE_BEAN_ADAPTER_CLASS != null) {
                field = ReflectionUtils.findField(DISPOSABLE_BEAN_ADAPTER_CLASS, "bean");
            }
            BEAN_FIELD = field;
        }

        private final String beanName;
        private final DisposableBean delegate;
        private final BiConsumer<String, Object> destroyedCallback;

        DecoratingDisposableBean(String beanName, DisposableBean delegate,
                                 BiConsumer<String, Object> destroyedCallback) {
            this.beanName = beanName;
            this.delegate = delegate;
            this.destroyedCallback = destroyedCallback;
        }

        @Override
        public void destroy() throws Exception {
            this.delegate.destroy();
            if (BEAN_FIELD != null) {
                ReflectionUtils.makeAccessible(BEAN_FIELD);
                Object bean = BEAN_FIELD.get(this.delegate);
                this.destroyedCallback.accept(this.beanName, bean);
            }
        }
    }
}
