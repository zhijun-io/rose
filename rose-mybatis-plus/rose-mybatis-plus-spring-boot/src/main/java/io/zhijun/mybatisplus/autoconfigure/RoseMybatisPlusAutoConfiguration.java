package io.zhijun.mybatisplus.autoconfigure;

import io.zhijun.mybatisplus.permission.DataPermissionInterceptorRegistrar;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;

import io.zhijun.mybatisplus.audit.AuditMetaObjectHandler;
import io.zhijun.mybatisplus.audit.CurrentAuditorProvider;
import io.zhijun.mybatisplus.crypto.DefaultFieldEncryptor;
import io.zhijun.mybatisplus.crypto.EncryptionKeyResolver;
import io.zhijun.mybatisplus.crypto.FieldEncryptInterceptor;
import io.zhijun.mybatisplus.crypto.FieldEncryptor;
import io.zhijun.mybatisplus.permission.DataPermissionConditionResolver;
import io.zhijun.mybatisplus.permission.DataPermissionPrincipalResolver;
import io.zhijun.mybatisplus.permission.RoseDataPermissionHandler;
import io.zhijun.mybatisplus.tenant.RoseTenantLineHandler;
import io.zhijun.mybatisplus.tenant.TenantIdSupplier;
import io.zhijun.mybatisplus.tenant.TenantLineInnerInterceptorRegistrar;
import io.zhijun.mybatisplus.spring.config.MyBatisPlusExtensionConfiguration;

/**
 * Auto-configuration for rose-mybatis-plus-core.
 */
@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class, MetaObjectHandler.class})
@ConditionalOnMybatisPlusEnabled
@EnableConfigurationProperties({EncryptorProperties.class, TenantLineProperties.class})
@Import({TenantContextTenantIdSupplierAutoConfiguration.class, MyBatisPlusExtensionConfiguration.class,
        SqlObservationAutoConfiguration.class})
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
    @ConditionalOnBean(TenantIdSupplier.class)
    @ConditionalOnProperty(prefix = TenantLineProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
    @ConditionalOnMissingBean(TenantLineHandler.class)
    public RoseTenantLineHandler roseTenantLineHandler(TenantIdSupplier tenantIdSupplier,
            TenantLineProperties tenantLineProperties) {
        return new RoseTenantLineHandler(tenantIdSupplier, tenantLineProperties.getColumn(),
                tenantLineProperties.getIgnoreTables());
    }

    @Bean
    @ConditionalOnBean(RoseTenantLineHandler.class)
    public TenantLineInnerInterceptorRegistrar tenantLineInnerInterceptorRegistrar(
            RoseTenantLineHandler roseTenantLineHandler) {
        return new TenantLineInnerInterceptorRegistrar(roseTenantLineHandler);
    }
}
