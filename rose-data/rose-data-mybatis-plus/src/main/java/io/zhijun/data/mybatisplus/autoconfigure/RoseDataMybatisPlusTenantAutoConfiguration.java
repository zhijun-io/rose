package io.zhijun.data.mybatisplus.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.data.mybatisplus.tenant.TenantContextTenantIdSupplier;
import io.zhijun.data.mybatisplus.tenant.TenantIdSupplier;

/**
 * Optional tenant bridge when rose-multitenancy is on the classpath.
 */
@Configuration
@ConditionalOnClass(name = "io.zhijun.multitenancy.core.context.TenantContext")
public class RoseDataMybatisPlusTenantAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantIdSupplier.class)
    public TenantIdSupplier tenantContextTenantIdSupplier() {
        return new TenantContextTenantIdSupplier();
    }
}
