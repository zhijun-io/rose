package io.zhijun.data.mybatisplus.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import io.zhijun.data.mybatisplus.audit.AuditMetaObjectHandler;
import io.zhijun.data.mybatisplus.audit.CurrentAuditorProvider;
import io.zhijun.data.mybatisplus.crypto.DefaultFieldEncryptor;
import io.zhijun.data.mybatisplus.crypto.EncryptionKeyResolver;
import io.zhijun.data.mybatisplus.crypto.FieldEncryptInterceptor;
import io.zhijun.data.mybatisplus.crypto.FieldEncryptor;
import io.zhijun.data.mybatisplus.permission.DataPermissionConditionResolver;
import io.zhijun.data.mybatisplus.permission.DataPermissionPrincipalResolver;
import io.zhijun.data.mybatisplus.permission.RoseDataPermissionHandler;
import io.zhijun.data.mybatisplus.properties.EncryptorProperties;

/**
 * Auto-configuration for rose-data-mybatis-plus.
 */
@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class, MetaObjectHandler.class})
@EnableConfigurationProperties(EncryptorProperties.class)
public class RoseDataMybatisPlusAutoConfiguration {

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
    public DataPermissionInnerInterceptorRegistrar dataPermissionInnerInterceptorRegistrar(
            DataPermissionHandler dataPermissionHandler) {
        return new DataPermissionInnerInterceptorRegistrar(dataPermissionHandler);
    }
}
