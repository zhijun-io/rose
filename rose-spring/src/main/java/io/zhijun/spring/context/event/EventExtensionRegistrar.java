package io.zhijun.spring.context.event;

import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.beans.BeanSource;
import io.zhijun.spring.context.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

import static io.zhijun.spring.beans.BeanSource.registerBeans;
import static io.zhijun.spring.beans.factory.BeanFactoryUtils.getBeanDefinition;
import static io.zhijun.spring.context.event.EnableEventExtension.NO_EXECUTOR;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * 事件扩展注册器。
 */
class EventExtensionRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableEventExtension> {

    private static final Logger logger = LoggerFactory.getLogger(EventExtensionRegistrar.class);

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableEventExtension> annotationAttributes) {
        registerApplicationEventMulticaster(metadata, registry, annotationAttributes);
    }

    void registerApplicationEventMulticaster(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                             ResolvablePlaceholderAnnotationAttributes<EnableEventExtension> annotationAttributes) {

        EventExtensionAttributes attributes = new EventExtensionAttributes(annotationAttributes);

        boolean intercepted = attributes.isIntercepted();
        String executorForListener = attributes.getExecutorForListener();

        boolean associatedExecutorBean = !NO_EXECUTOR.equals(executorForListener);

        String beanName = APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

        if (!intercepted && !associatedExecutorBean) {
            if (logger.isInfoEnabled()) {
                logger.info("ApplicationEventMulticaster will not be registered, caused by {} on '{}'",
                        annotationAttributes, metadata.getClassName());
            }
            return;
        }

        final BeanDefinition existedBeanDefinition = getBeanDefinition(registry, beanName);

        final AbstractBeanDefinition targetBeanDefinition;

        if (existedBeanDefinition == null) {
            targetBeanDefinition = buildApplicationEventMulticasterBeanDefinition(intercepted, executorForListener);
        } else {
            if (isSameBeanDefinition(existedBeanDefinition, intercepted, executorForListener)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Same @EnableEventExtension already registered on '{}'",
                            existedBeanDefinition.getSource());
                }
                return;
            }
            targetBeanDefinition = rebuildApplicationEventMulticasterBeanDefinition(intercepted, existedBeanDefinition, beanName, registry);
        }

        if (intercepted) {
            BeanSource[] sources = attributes.getSources();
            registerApplicationEventInterceptors(registry, sources);
            registerApplicationListenerInterceptors(registry, sources);
        }

        associateExecutorBeanIfRequired(targetBeanDefinition, associatedExecutorBean, executorForListener);
        registry.registerBeanDefinition(beanName, targetBeanDefinition);
    }

    private boolean isSameBeanDefinition(BeanDefinition beanDefinition, boolean intercepted, String executorForListener) {
        if (!Objects.equals(beanDefinition.getAttribute(EventExtensionAttributes.INTERCEPTED_ATTRIBUTE_NAME), intercepted)) {
            return false;
        }
        if (!Objects.equals(beanDefinition.getAttribute(EventExtensionAttributes.EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME), executorForListener)) {
            return false;
        }
        return true;
    }

    private AbstractBeanDefinition rebuildApplicationEventMulticasterBeanDefinition(boolean intercepted,
                                                                                    BeanDefinition existedBeanDefinition,
                                                                                    String beanName,
                                                                                    BeanDefinitionRegistry registry) {
        final AbstractBeanDefinition targetBeanDefinition;
        if (intercepted) {
            registry.removeBeanDefinition(beanName);
            String resetBeanName = InterceptingApplicationEventMulticasterProxy.DEFAULT_RESET_BEAN_NAME;
            registry.registerBeanDefinition(resetBeanName, existedBeanDefinition);
            targetBeanDefinition = new RootBeanDefinition(InterceptingApplicationEventMulticasterProxy.class);
        } else {
            targetBeanDefinition = (AbstractBeanDefinition) existedBeanDefinition;
        }
        return targetBeanDefinition;
    }

    private AbstractBeanDefinition buildApplicationEventMulticasterBeanDefinition(boolean intercepted, String executorForListener) {
        Class<?> beanClass = intercepted ? InterceptingApplicationEventMulticaster.class : SimpleApplicationEventMulticaster.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
        beanDefinition.setAttribute(EventExtensionAttributes.INTERCEPTED_ATTRIBUTE_NAME, intercepted);
        beanDefinition.setAttribute(EventExtensionAttributes.EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME, executorForListener);
        return beanDefinition;
    }

    private void registerApplicationEventInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, ApplicationEventInterceptor.class);
    }

    private void registerApplicationListenerInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, ApplicationListenerInterceptor.class);
    }

    private void associateExecutorBeanIfRequired(AbstractBeanDefinition beanDefinition,
                                                 boolean associatedExecutorBean,
                                                 String executorBeanName) {
        if (associatedExecutorBean) {
            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
            propertyValues.addPropertyValue("taskExecutor", new RuntimeBeanReference(executorBeanName));
        }
    }
}
