package io.zhijun.spring.context.annotation;

import io.zhijun.spring.beans.factory.support.BeanRegistrar;
import io.zhijun.spring.context.config.AutoRegistrationBean;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.zhijun.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.zhijun.spring.context.annotation.EnableAutoRegistrationBean.BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
import static io.zhijun.spring.context.config.AutoRegistrationBean.getAutoRegisteredPropertyName;
import static io.zhijun.spring.core.io.SpringFactoriesLoaderUtils.loadFactories;

class AutoRegistrationBeanRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAutoRegistrationBean> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableAutoRegistrationBean> annotationAttributes) {
        List<AutoRegistrationBean> autoRegistrationBeans = loadFactories(AutoRegistrationBean.class, super.classLoader);
        registerAutoRegisteredBeans(autoRegistrationBeans, registry);
    }

    @Override
    protected boolean isEnabled(AnnotationMetadata metadata) {
        if (!isEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("The @EnableAutoRegistrationBean was disabled by property[{} = false]",
                    BEANS_AUTO_REGISTERED_PROEPRTY_NAME);
            }
            return false;
        }
        return super.isEnabled(metadata);
    }

    private boolean isEnabled() {
        return this.environment.getProperty(BEANS_AUTO_REGISTERED_PROEPRTY_NAME, boolean.class, DEFAULT_AUTO_REGISTERED_VALUE);
    }

    private void registerAutoRegisteredBeans(List<AutoRegistrationBean> autoRegistrationBeans, BeanDefinitionRegistry registry) {
        for (AutoRegistrationBean autoRegistrationBean : autoRegistrationBeans) {
            registerAutoRegisteredBean(autoRegistrationBean, registry);
        }
    }

    private void registerAutoRegisteredBean(AutoRegistrationBean autoRegistrationBean, BeanDefinitionRegistry registry) {
        String beanName = autoRegistrationBean.getBeanName();
        if (registry.containsBeanDefinition(beanName)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The BeanDefinition[{}] was registered already!", autoRegistrationBean.getDescription());
            }
            return;
        }

        if (!autoRegistrationBean.isAutoRegistered(this.environment)) {
            if (logger.isTraceEnabled()) {
                logger.trace("The Bean[{}] is not auto registered because of the property[{} = false]",
                    autoRegistrationBean.getDescription(), getAutoRegisteredPropertyName(beanName));
            }
            return;
        }

        Class<AutoRegistrationBean> beanType = (Class<AutoRegistrationBean>) autoRegistrationBean.getBeanType();
        String scope = autoRegistrationBean.getScope();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanType, () -> autoRegistrationBean)
            .setScope(scope);

        autoRegistrationBean.customize(beanDefinitionBuilder);

        BeanRegistrar.registerBeanDefinition(registry, beanName, beanDefinitionBuilder.getBeanDefinition());
    }
}
