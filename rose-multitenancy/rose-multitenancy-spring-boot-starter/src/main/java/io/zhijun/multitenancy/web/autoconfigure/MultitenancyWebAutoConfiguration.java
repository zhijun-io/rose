package io.zhijun.multitenancy.web.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

import io.zhijun.multitenancy.core.autoconfigure.MultitenancyCoreAutoConfiguration;
import io.zhijun.multitenancy.web.filter.TenantContextFilter;

/**
 * Auto-configuration for web multitenancy.
 */
@AutoConfiguration(after = MultitenancyCoreAutoConfiguration.class)
@ConditionalOnClass(TenantContextFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({ HttpTenantResolutionConfiguration.class, WebMvcConfiguration.class })
public final class MultitenancyWebAutoConfiguration {}
