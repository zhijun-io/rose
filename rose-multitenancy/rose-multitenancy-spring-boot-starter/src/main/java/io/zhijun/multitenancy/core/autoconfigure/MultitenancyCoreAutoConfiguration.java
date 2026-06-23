package io.zhijun.multitenancy.core.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.zhijun.multitenancy.core.cache.DefaultTenantKeyGenerator;
import io.zhijun.multitenancy.core.cache.TenantKeyGenerator;
import io.zhijun.multitenancy.core.context.resolver.FixedTenantResolver;

/**
 * Auto-configuration for core multitenancy.
 */
@AutoConfiguration
@EnableConfigurationProperties({FixedTenantResolutionProperties.class, MultitenancyAsyncProperties.class})
@Import({ TenantDetailsConfiguration.class, TenantObservabilityConfiguration.class, MultitenancyAsyncConfiguration.class })
public final class MultitenancyCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantKeyGenerator.class)
    DefaultTenantKeyGenerator tenantKeyGenerator() {
        return new DefaultTenantKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FixedTenantResolutionProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    FixedTenantResolver fixedTenantResolver(FixedTenantResolutionProperties fixedTenantResolutionProperties) {
        return new FixedTenantResolver(fixedTenantResolutionProperties.getTenantIdentifier());
    }

}
