package io.zhijun.mybatisplus.boot.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.mybatisplus.boot.autoconfigure.multitenancy.MultitenancyAutoConfiguration;
import io.zhijun.mybatisplus.boot.autoconfigure.multitenancy.MultitenancyLineProperties;
import io.zhijun.mybatisplus.core.audit.AuditMetaObjectHandler;
import io.zhijun.mybatisplus.core.audit.CurrentAuditorProvider;
import io.zhijun.mybatisplus.core.crypto.DefaultFieldEncryptor;
import io.zhijun.mybatisplus.core.crypto.EncryptionKeyResolver;
import io.zhijun.mybatisplus.core.crypto.FieldEncryptInterceptor;
import io.zhijun.mybatisplus.core.crypto.FieldEncryptor;
import io.zhijun.mybatisplus.core.permission.DataPermissionConditionResolver;
import io.zhijun.mybatisplus.core.permission.DataPermissionInterceptorRegistrar;
import io.zhijun.mybatisplus.core.permission.DataPermissionPrincipalResolver;
import io.zhijun.mybatisplus.core.permission.RoseDataPermissionHandler;

/**
 * Auto-configuration for rose-mybatis-plus-core.
 */
@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class, MetaObjectHandler.class})
@ConditionalOnMybatisPlusEnabled
@EnableConfigurationProperties({EncryptorProperties.class, MultitenancyLineProperties.class})
@Import({
    MultitenancyAutoConfiguration.class,
    io.zhijun.mybatisplus.spring.extension.MyBatisPlusExtensionConfiguration.class
})
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
        return secretRef -> {
            if ("default".equals(secretRef)) {
                return properties.getPassword();
            }
            throw new IllegalArgumentException("No secret configured for secretRef '" + secretRef + "'. "
                    + "Provide a custom EncryptionKeyResolver bean to handle non-default secretRefs.");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptInterceptor fieldEncryptInterceptor(
            FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        return new FieldEncryptInterceptor(encryptor, keyResolver);
    }

    @Bean
    @ConditionalOnBean({DataPermissionPrincipalResolver.class, DataPermissionConditionResolver.class})
    @ConditionalOnMissingBean(DataPermissionHandler.class)
    public DataPermissionHandler roseDataPermissionHandler(
            DataPermissionPrincipalResolver principalResolver, DataPermissionConditionResolver conditionResolver) {
        return new RoseDataPermissionHandler(principalResolver, conditionResolver);
    }

    @Bean
    @ConditionalOnBean(DataPermissionHandler.class)
    public DataPermissionInterceptorRegistrar dataPermissionInterceptorRegistrar(
            DataPermissionHandler dataPermissionHandler) {
        return new DataPermissionInterceptorRegistrar(dataPermissionHandler);
    }
}
