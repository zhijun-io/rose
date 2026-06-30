package io.zhijun.multitenancy.boot.autoconfigure.web;

import io.zhijun.multitenancy.boot.autoconfigure.MultitenancyCoreAutoConfiguration;
import io.zhijun.multitenancy.spring.web.filter.TenantContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for web multitenancy.
 */
@AutoConfiguration(after = MultitenancyCoreAutoConfiguration.class)
@ConditionalOnClass(TenantContextFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({HttpTenantResolutionConfiguration.class, WebMvcConfiguration.class})
public final class MultitenancyWebAutoConfiguration {}
