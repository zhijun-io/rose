package io.zhijun.multitenancy.boot.autoconfigure.logging;

import io.zhijun.multitenancy.spring.logging.MdcTenantEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for multitenancy MDC logging.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TenantLoggingProperties.class)
public class TenantLoggingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = TenantLoggingProperties.CONFIG_PREFIX,
            name = "mdc.enabled",
            havingValue = "true",
            matchIfMissing = true)
    MdcTenantEventListener mdcTenantEventListener(TenantLoggingProperties tenantLoggingProperties) {
        return new MdcTenantEventListener(tenantLoggingProperties.getMdc().getKeyName());
    }
}
