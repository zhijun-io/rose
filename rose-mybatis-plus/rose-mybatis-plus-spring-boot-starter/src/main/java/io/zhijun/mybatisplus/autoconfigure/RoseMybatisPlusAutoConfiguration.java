package io.zhijun.mybatisplus.autoconfigure;

import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;
import io.zhijun.mybatisplus.permission.DataPermissionInterceptorRegistrar;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import io.zhijun.mybatisplus.audit.AuditMetaObjectHandler;
import io.zhijun.mybatisplus.audit.CurrentAuditorProvider;
import io.zhijun.mybatisplus.crypto.DefaultFieldEncryptor;
import io.zhijun.mybatisplus.crypto.EncryptionKeyResolver;
import io.zhijun.mybatisplus.crypto.FieldEncryptInterceptor;
import io.zhijun.mybatisplus.crypto.FieldEncryptor;
import io.zhijun.mybatisplus.permission.DataPermissionConditionResolver;
import io.zhijun.mybatisplus.permission.DataPermissionPrincipalResolver;
import io.zhijun.mybatisplus.permission.RoseDataPermissionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for rose-mybatis-plus-core.
 */
@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class, MetaObjectHandler.class})
@ConditionalOnMybatisPlusEnabled
@EnableConfigurationProperties(EncryptorProperties.class)
public class RoseMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CurrentAuditorProvider currentAuditorProvider() {
        return () -> null;
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler auditMetaObjectHandler(CurrentAuditorProvider auditorProvider) {
        return new AuditMetaObjectHandler(auditorProvider);
    }

    @Bean
    @ConditionalOnMissingBean(FieldEncryptor.class)
    public FieldEncryptor fieldEncryptor() {
        return new DefaultFieldEncryptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptionKeyResolver encryptionKeyResolver(EncryptorProperties properties) {
        return secretRef -> "default".equals(secretRef) ? properties.getPassword() : null;
    }

    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptInterceptor fieldEncryptInterceptor(FieldEncryptor encryptor,
            EncryptionKeyResolver keyResolver) {
        return new FieldEncryptInterceptor(encryptor, keyResolver);
    }

    @Bean
    @ConditionalOnBean({DataPermissionPrincipalResolver.class, DataPermissionConditionResolver.class})
    @ConditionalOnMissingBean(DataPermissionHandler.class)
    public DataPermissionHandler roseDataPermissionHandler(DataPermissionPrincipalResolver principalResolver,
            DataPermissionConditionResolver conditionResolver) {
        return new RoseDataPermissionHandler(principalResolver, conditionResolver);
    }

    @Bean
    @ConditionalOnBean(DataPermissionHandler.class)
    public DataPermissionInterceptorRegistrar dataPermissionInterceptorRegistrar(
            DataPermissionHandler dataPermissionHandler) {
        return new DataPermissionInterceptorRegistrar(dataPermissionHandler);
    }

    @Bean
    public MybatisPlusInterceptorCustomizerBeanPostProcessor mybatisPlusInterceptorCustomizerBeanPostProcessor() {
        // Spring-bean customizers are resolved lazily by the BPP itself via BeanFactory;
        // here we only supply spring.factories-discovered customizers (third-party jar extensions).
        List<MybatisPlusInterceptorCustomizer> factoryCustomizers =
                SpringFactoriesLoaderUtils.loadFactories(MybatisPlusInterceptorCustomizer.class);
        return new MybatisPlusInterceptorCustomizerBeanPostProcessor(factoryCustomizers);
    }
}
