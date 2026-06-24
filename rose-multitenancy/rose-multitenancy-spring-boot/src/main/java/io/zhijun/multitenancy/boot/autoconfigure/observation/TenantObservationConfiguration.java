package io.zhijun.multitenancy.boot.autoconfigure.observation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.multitenancy.spring.observation.MdcTenantEventListener;
import io.zhijun.multitenancy.core.observation.TenantObservationFilter;

/**
 * Configuration for multitenancy observation: Micrometer observations and MDC logging.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ TenantObservationProperties.class, TenantLoggingProperties.class })
public class TenantObservationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = TenantObservationProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
            matchIfMissing = true)
    TenantObservationFilter tenantObservationFilter(TenantObservationProperties tenantObservationProperties) {
        return new TenantObservationFilter(tenantObservationProperties.getKeyName(),
                tenantObservationProperties.getCardinality());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = TenantLoggingProperties.CONFIG_PREFIX, name = "mdc.enabled", havingValue = "true",
            matchIfMissing = true)
    MdcTenantEventListener mdcTenantEventListener(TenantLoggingProperties tenantLoggingProperties) {
        return new MdcTenantEventListener(tenantLoggingProperties.getMdc().getKeyName());
    }

}
