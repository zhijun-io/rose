package io.zhijun.multitenancy.boot.autoconfigure.detail;

import io.zhijun.multitenancy.core.detail.DefaultTenantVerifier;
import io.zhijun.multitenancy.core.detail.FormatTenantVerifier;
import io.zhijun.multitenancy.core.detail.TenantDetailsService;
import io.zhijun.multitenancy.core.detail.TenantVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TenantDetailsProperties.class)
public final class TenantDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantDetailsService.class)
    @ConditionalOnProperty(prefix = TenantDetailsProperties.CONFIG_PREFIX, name = "source", havingValue = "properties")
    PropertiesTenantDetailsService tenantDetailsService(TenantDetailsProperties tenantDetailsProperties) {
        return new PropertiesTenantDetailsService(tenantDetailsProperties);
    }

    @Bean
    @ConditionalOnMissingBean(TenantVerifier.class)
    @ConditionalOnBean(TenantDetailsService.class)
    DefaultTenantVerifier tenantVerifier(TenantDetailsService tenantDetailsService) {
        return new DefaultTenantVerifier(tenantDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean(TenantVerifier.class)
    FormatTenantVerifier formatTenantVerifier() {
        return new FormatTenantVerifier();
    }
}
