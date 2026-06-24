package io.zhijun.mybatisplus.core.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.mybatisplus.multitenancy.TenantContextTenantIdSupplier;
import io.zhijun.mybatisplus.multitenancy.TenantIdSupplier;

/**
 * Registers a default {@link TenantIdSupplier} when {@link TenantContext} is available.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TenantContext.class)
class TenantContextTenantIdSupplierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantIdSupplier.class)
    TenantIdSupplier tenantContextTenantIdSupplier() {
        return new TenantContextTenantIdSupplier();
    }

}
