package io.zhijun.mybatisplus.boot.autoconfigure.multitenancy;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.mybatisplus.core.multitenancy.RoseTenantLineHandler;
import io.zhijun.mybatisplus.core.multitenancy.TenantContextTenantIdSupplier;
import io.zhijun.mybatisplus.core.multitenancy.TenantIdSupplier;
import io.zhijun.mybatisplus.core.multitenancy.TenantLineInnerInterceptorRegistrar;

/**
 * Registers a default {@link TenantIdSupplier} when {@link TenantContext} is available.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TenantContext.class)
@ConditionalOnProperty(prefix = MultitenancyLineProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class MultitenancyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantIdSupplier.class)
    TenantIdSupplier tenantContextTenantIdSupplier() {
        return new TenantContextTenantIdSupplier();
    }

    @Bean
    @ConditionalOnBean(TenantIdSupplier.class)
    @ConditionalOnMissingBean(TenantLineHandler.class)
    public RoseTenantLineHandler roseTenantLineHandler(TenantIdSupplier tenantIdSupplier,
                                                       MultitenancyLineProperties multitenancyLineProperties) {
        return new RoseTenantLineHandler(tenantIdSupplier, multitenancyLineProperties.getColumn(),
                multitenancyLineProperties.getIgnoreTables());
    }

    @Bean
    @ConditionalOnBean(RoseTenantLineHandler.class)
    public TenantLineInnerInterceptorRegistrar tenantLineInnerInterceptorRegistrar(
            RoseTenantLineHandler roseTenantLineHandler) {
        return new TenantLineInnerInterceptorRegistrar(roseTenantLineHandler);
    }
}
