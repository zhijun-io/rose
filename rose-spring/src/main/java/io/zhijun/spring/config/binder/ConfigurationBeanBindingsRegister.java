package io.zhijun.spring.config.binder;

import io.zhijun.spring.context.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * The {@link ImportBeanDefinitionRegistrar Registrar class} for {@link EnableConfigurationBeanBindings}
 *
 * @since 1.0.0
 */
class ConfigurationBeanBindingsRegister extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableConfigurationBeanBindings> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableConfigurationBeanBindings> annotationAttributes) {

        ConfigurationBeanBindingRegistrar registrar = new ConfigurationBeanBindingRegistrar();

        registrar.setEnvironment(getEnvironment());
        registrar.setBeanFactory(getBeanFactory());

        for (AnnotationAttributes attributes : annotationAttributes.getAnnotationArray("value")) {
            registrar.registerConfigurationBeanDefinitions(attributes, registry);
        }
    }
}
