package io.zhijun.spring.webmvc.annotation;

import io.zhijun.spring.context.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.webmvc.ReversedProxyHandlerMapping;
import io.zhijun.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.zhijun.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.zhijun.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.zhijun.spring.webmvc.method.InterceptingHandlerMethodProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

import static io.zhijun.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring WebMVC Extension
 *
 * @see EnableWebMvcExtension
 * @see WebMvcExtensionConfiguration
 * @see InterceptingHandlerMethodProcessor
 * @see StoringRequestBodyArgumentAdvice
 * @see StoringResponseBodyReturnValueAdvice
 * @see ReversedProxyHandlerMapping
 * @since 1.0.0
 */
class WebMvcExtensionBeanDefinitionRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableWebMvcExtension> {

    private static final Class<? extends HandlerInterceptor>[] ALL_HANDLER_INTERCEPTOR_CLASSES = new Class[]{HandlerInterceptor.class};

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator,
                                        ResolvablePlaceholderAnnotationAttributes<EnableWebMvcExtension> annotationAttributes) {

        registerWebMvcExtensionConfiguration(registry);

        registerInterceptingHandlerMethodProcessor(annotationAttributes, registry);

        registerHandlerInterceptors(annotationAttributes, registry);

        registerStoringRequestBodyArgumentAdvice(annotationAttributes, registry);

        registerStoringResponseBodyReturnValueAdvice(annotationAttributes, registry);

        registerReversedProxyHandlerMapping(annotationAttributes, registry);
    }

    private void registerWebMvcExtensionConfiguration(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WebMvcExtensionConfiguration.class);
    }

    private void registerInterceptingHandlerMethodProcessor(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = annotationAttributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = InterceptingHandlerMethodProcessor.BEAN_NAME;
            registerBeanDefinition(registry, beanName, InterceptingHandlerMethodProcessor.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.interceptHandlerMethods() = {}", interceptHandlerMethods);
        }
    }

    private void registerHandlerInterceptors(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        Class<? extends HandlerInterceptor>[] interceptorClasses = resolveHandlerInterceptorClasses(annotationAttributes);
        if (isNotEmpty(interceptorClasses)) {
            registerLazyCompositeHandlerInterceptor(registry, interceptorClasses);
            registerInterceptors(registry, interceptorClasses);
        }
    }

    private Class<? extends HandlerInterceptor>[] resolveHandlerInterceptorClasses(AnnotationAttributes annotationAttributes) {
        boolean registerHandlerInterceptors = annotationAttributes.getBoolean("registerHandlerInterceptors");
        @SuppressWarnings("unchecked")
        Class<? extends HandlerInterceptor>[] handlerInterceptors =
                (Class<? extends HandlerInterceptor>[]) annotationAttributes.getClassArray("handlerInterceptors");
        Class<? extends HandlerInterceptor>[] result = registerHandlerInterceptors ?
                ALL_HANDLER_INTERCEPTOR_CLASSES : handlerInterceptors;
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.registerHandlerInterceptors() = {} , handlerInterceptors() = {} , resolved = {}",
                    registerHandlerInterceptors, handlerInterceptors, result);
        }
        return result;
    }

    private void registerLazyCompositeHandlerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>... interceptorClasses) {
        AbstractBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(LazyCompositeHandlerInterceptor.class);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(interceptorClasses);
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    private void registerInterceptors(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>[] interceptorClasses) {
        if (Arrays.equals(ALL_HANDLER_INTERCEPTOR_CLASSES, interceptorClasses)) {
            return;
        }
        for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
            registerBeanDefinition(registry, interceptorClass);
        }
    }

    private void registerStoringRequestBodyArgumentAdvice(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean storeRequestBodyArgument = annotationAttributes.getBoolean("storeRequestBodyArgument");
        if (storeRequestBodyArgument) {
            registerBeanDefinition(registry, StoringRequestBodyArgumentAdvice.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.storeRequestBodyArgument() = {}", storeRequestBodyArgument);
        }
    }

    private void registerStoringResponseBodyReturnValueAdvice(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean storeResponseBodyReturnValue = annotationAttributes.getBoolean("storeResponseBodyReturnValue");
        if (storeResponseBodyReturnValue) {
            registerBeanDefinition(registry, StoringResponseBodyReturnValueAdvice.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.storeResponseBodyReturnValue() = {}", storeResponseBodyReturnValue);
        }
    }

    private void registerReversedProxyHandlerMapping(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean reversedProxyHandlerMapping = annotationAttributes.getBoolean("reversedProxyHandlerMapping");
        if (reversedProxyHandlerMapping) {
            registerBeanDefinition(registry, ReversedProxyHandlerMapping.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.reversedProxyHandlerMapping() = {}", reversedProxyHandlerMapping);
        }
    }

    // ---- 静态工具 ----

    private static void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass) {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(beanClass);
        registry.registerBeanDefinition(beanClass.getName(), bd);
    }

    private static void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass) {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(beanClass);
        registry.registerBeanDefinition(beanName, bd);
    }

    private static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }
}
