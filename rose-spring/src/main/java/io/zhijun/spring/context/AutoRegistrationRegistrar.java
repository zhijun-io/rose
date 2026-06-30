package io.zhijun.spring.context;

import io.zhijun.core.spi.SpiLoader;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * {@link EnableAutoRegistration @EnableAutoRegistration} 的实现。
 * <p>
 * 通过 {@link SpiLoader} 发现所有 {@link AutoRegistrationBean} 实现，
 * 按优先级排序后注册为 Spring Bean。
 */
class AutoRegistrationRegistrar
        extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAutoRegistration> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata,
                                           BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableAutoRegistration> attributes) {
        List<AutoRegistrationBean> beans = SpiLoader.defaults().loadAll(AutoRegistrationBean.class);
        AnnotationAwareOrderComparator.sort(beans);

        for (AutoRegistrationBean bean : beans) {
            if (!bean.isAutoRegistered(environment)) {
                logger.debug("Auto-registration bean [{}] disabled, skipped", bean.getBeanName());
                continue;
            }
            String beanName = bean.getBeanName();
            if (registry.containsBeanDefinition(beanName)) {
                logger.debug("Bean [{}] already registered, skipped", beanName);
                continue;
            }
            GenericBeanDefinition bd = new GenericBeanDefinition();
            bd.setBeanClass(bean.getClass());
            bd.setInstanceSupplier(() -> bean);
            bd.setScope(bean.getScope());
            registry.registerBeanDefinition(beanName, bd);
            logger.info("Auto-registered bean [{}] of type [{}]", beanName, bean.getClass().getName());
        }
    }
}
