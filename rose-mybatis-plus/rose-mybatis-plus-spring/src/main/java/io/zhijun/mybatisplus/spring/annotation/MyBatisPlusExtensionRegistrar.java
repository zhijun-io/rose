package io.zhijun.mybatisplus.spring.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

/**
 * {@link ImportBeanDefinitionRegistrar} that registers a
 * {@link MybatisPlusInterceptorCustomizerBeanPostProcessor} bean.
 * <p>
 * The BPP is supplied with customizers from two sources:
 * <ul>
 *     <li>Spring beans of type {@link MybatisPlusInterceptorCustomizer} (resolved lazily at runtime)</li>
 *     <li>{@code spring.factories}-discovered implementations (for third-party jars)</li>
 * </ul>
 * <p>
 * Because Spring beans are not yet instantiated during registrar execution, the BPP itself
 * resolves bean customizers via {@code ObjectProvider} at construction. The spring.factories
 * customizers are loaded eagerly here and passed as constructor argument.
 *
 * @see EnableMyBatisPlusExtension
 * @since 0.0.0.2
 */
public class MyBatisPlusExtensionRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private static final String BPP_BEAN_NAME = "mybatisPlusInterceptorCustomizerBeanPostProcessor";

    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(BPP_BEAN_NAME)) {
            return;
        }

        List<MybatisPlusInterceptorCustomizer> factoryCustomizers =
                SpringFactoriesLoaderUtils.loadFactories(beanFactory, MybatisPlusInterceptorCustomizer.class);

        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(MybatisPlusInterceptorCustomizerBeanPostProcessor.class);
        builder.addConstructorArgValue(factoryCustomizers);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        registry.registerBeanDefinition(BPP_BEAN_NAME, builder.getBeanDefinition());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
