package io.zhijun.spring.core.binder.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Delegates each element of {@link EnableConfigurationBeanBindings#value()} to
 * {@link ConfigurationBeanBindingRegistrar}.
 */
public class ConfigurationBeanBindingsRegistrar
        implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableConfigurationBeanBindings.class.getName()));
        if (attributes == null) {
            return;
        }
        ConfigurationBeanBindingRegistrar delegate = new ConfigurationBeanBindingRegistrar();
        delegate.setEnvironment(environment);
        delegate.setBeanFactory(beanFactory);
        for (AnnotationAttributes element : attributes.getAnnotationArray("value")) {
            delegate.registerConfigurationBeanDefinitions(element, registry);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
