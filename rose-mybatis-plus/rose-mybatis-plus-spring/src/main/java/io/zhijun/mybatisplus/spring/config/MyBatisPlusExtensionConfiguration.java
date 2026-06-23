package io.zhijun.mybatisplus.spring.config;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.spring.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

/**
 * Registers infrastructure beans shared by Spring Boot auto-configuration and
 * {@link io.zhijun.mybatisplus.spring.annotation.EnableMyBatisPlusExtension}.
 */
@Configuration(proxyBeanMethods = false)
public final class MyBatisPlusExtensionConfiguration {

    public static final String BPP_BEAN_NAME = "mybatisPlusInterceptorCustomizerBeanPostProcessor";

    @Bean(name = BPP_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    MybatisPlusInterceptorCustomizerBeanPostProcessor mybatisPlusInterceptorCustomizerBeanPostProcessor(
            BeanFactory beanFactory) {
        List<MybatisPlusInterceptorCustomizer> factoryCustomizers =
                SpringFactoriesLoaderUtils.loadFactories(beanFactory, MybatisPlusInterceptorCustomizer.class);
        return new MybatisPlusInterceptorCustomizerBeanPostProcessor(factoryCustomizers);
    }

}
