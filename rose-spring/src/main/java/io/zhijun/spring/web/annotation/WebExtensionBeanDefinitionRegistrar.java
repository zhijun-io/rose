package io.zhijun.spring.web.annotation;

import io.zhijun.spring.beans.BeanSource;
import io.zhijun.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.zhijun.spring.web.event.WebEventPublisher;
import io.zhijun.spring.web.metadata.CompositeWebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.WebEndpointMappingFactory;
import io.zhijun.spring.web.metadata.WebEndpointMappingFilter;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistrar;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.WebEndpointMappingResolver;
 import io.zhijun.spring.web.method.support.HandlerMethodAdvice;
import io.zhijun.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.zhijun.spring.web.method.support.HandlerMethodInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Map.Entry;

import static io.zhijun.spring.beans.BeanSource.registerBeans;
import static io.zhijun.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.zhijun.spring.web.method.support.DelegatingHandlerMethodAdvice.BEAN_NAME;

/**
 * {@link EnableWebExtension} 的 {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar} 实现。
 *
 * @since 1.0.0
 */
public class WebExtensionBeanDefinitionRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableWebExtension> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableWebExtension> annotationAttributes) {

        BeanSource[] sources = (BeanSource[]) annotationAttributes.get("sources");

        registerWebEndpointMappings(annotationAttributes, registry, sources);
        registerInterceptHandlers(annotationAttributes, registry, sources);
        registerEventPublishingProcessor(annotationAttributes, registry);
    }

    private void registerWebEndpointMappings(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry, BeanSource[] sources) {
        boolean registerWebEndpointMappings = annotationAttributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerWebEndpointMappingResolvers(registry, sources);
            registerWebEndpointMappingRegistries(registry, sources);
            registerWebEndpointMappingFactories(registry, sources);
            registerWebEndpointMappingFilters(registry, sources);
            registerWebEndpointMappingRegistrar(registry);
        }
    }

    private void registerWebEndpointMappingResolvers(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingResolver.class);
    }

    private void registerWebEndpointMappingRegistries(BeanDefinitionRegistry registry, BeanSource[] sources) {
        Map<Class<?>, String> beanTypesAndNames = registerBeans(registry, sources, WebEndpointMappingRegistry.class);
        if (beanTypesAndNames.isEmpty()) {
            registerBeanDefinition(registry, null, SimpleWebEndpointMappingRegistry.class);
        } else {
            for (Entry<Class<?>, String> entry : beanTypesAndNames.entrySet()) {
                String beanName = entry.getValue();
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                beanDefinition.setPrimary(false);
            }
            registerBeanDefinition(registry, null, CompositeWebEndpointMappingRegistry.class);
        }
    }

    private void registerWebEndpointMappingFactories(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingFactory.class);
    }

    private void registerWebEndpointMappingFilters(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingFilter.class);
    }

    private void registerWebEndpointMappingRegistrar(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, null, WebEndpointMappingRegistrar.class);
    }

    private void registerInterceptHandlers(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry, BeanSource[] sources) {
        boolean interceptHandlerMethods = annotationAttributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            registerHandlerMethodAdvices(registry, sources);
            registerHandlerMethodArgumentInterceptors(registry, sources);
            registerHandlerMethodInterceptors(registry, sources);
        }
    }

    private void registerHandlerMethodAdvices(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodAdvice.class);
        registerDelegatingHandlerMethodAdvice(registry);
    }

    private void registerDelegatingHandlerMethodAdvice(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, DelegatingHandlerMethodAdvice.class);
    }

    private void registerHandlerMethodArgumentInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodArgumentInterceptor.class);
    }

    private void registerHandlerMethodInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodInterceptor.class);
    }

    private void registerEventPublishingProcessor(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean publishEvents = annotationAttributes.getBoolean("publishEvents");
        if (publishEvents) {
            registerBeanDefinition(registry, null, WebEventPublisher.class);
        }
    }
}
