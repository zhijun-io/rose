package io.zhijun.mybatisplus.spring.extension;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.io.support.SpringFactoriesLoader;

import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.spring.annotation.EnableMyBatisPlusExtension;

/**
 * Registers infrastructure beans shared by Spring Boot auto-configuration and
 * {@link EnableMyBatisPlusExtension}.
 */
@Configuration(proxyBeanMethods = false)
public final class MyBatisPlusExtensionConfiguration {

    public static final String BPP_BEAN_NAME = "mybatisPlusInterceptorCustomizerBeanPostProcessor";

    @Bean(name = BPP_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    MybatisPlusInterceptorCustomizerBeanPostProcessor mybatisPlusInterceptorCustomizerBeanPostProcessor(
            BeanFactory beanFactory) {
        ClassLoader classLoader = beanFactory instanceof ConfigurableBeanFactory
                ? ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader()
                : null;
        List<MybatisPlusInterceptorCustomizer> factoryCustomizers =
                SpringFactoriesLoader.loadFactories(MybatisPlusInterceptorCustomizer.class, classLoader);
        return new MybatisPlusInterceptorCustomizerBeanPostProcessor(factoryCustomizers);
    }
}
