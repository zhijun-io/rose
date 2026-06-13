package io.zhijun.multitenancy.web.autoconfigure;

import java.util.HashSet;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.multitenancy.core.autoconfigure.FixedTenantResolutionProperties;
import io.zhijun.multitenancy.core.context.resolvers.FixedTenantResolver;
import io.zhijun.multitenancy.core.observability.TenantObservationFilter;
import io.zhijun.multitenancy.core.tenantdetails.TenantVerifier;
import io.zhijun.multitenancy.web.context.filters.TenantContextFilter;
import io.zhijun.multitenancy.web.context.filters.TenantContextIgnorePathMatcher;
import io.zhijun.multitenancy.web.context.resolvers.CookieTenantResolver;
import io.zhijun.multitenancy.web.context.resolvers.HeaderTenantResolver;
import io.zhijun.multitenancy.web.context.resolvers.HttpRequestTenantResolver;

/**
 * Configuration for HTTP tenant resolution.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HttpTenantResolutionProperties.class)
@ConditionalOnProperty(prefix = HttpTenantResolutionProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public final class HttpTenantResolutionConfiguration {

    @Bean
    @ConditionalOnBean(FixedTenantResolver.class)
    @ConditionalOnProperty(prefix = FixedTenantResolutionProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    HttpRequestTenantResolver fixedHttpRequestTenantResolver(FixedTenantResolver fixedTenantResolver) {
        return new HttpRequestTenantResolver() {
            @Override
            public String resolveTenantIdentifier(javax.servlet.http.HttpServletRequest request) {
                return fixedTenantResolver.resolveTenantIdentifier(request);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(HttpRequestTenantResolver.class)
    @ConditionalOnProperty(prefix = HttpTenantResolutionProperties.CONFIG_PREFIX, name = "resolution-mode",
            havingValue = "header", matchIfMissing = true)
    HeaderTenantResolver headerTenantResolver(HttpTenantResolutionProperties httpTenantResolutionProperties) {
        return new HeaderTenantResolver(httpTenantResolutionProperties.getHeader().getHeaderName());
    }

    @Bean
    @ConditionalOnMissingBean(HttpRequestTenantResolver.class)
    @ConditionalOnProperty(prefix = HttpTenantResolutionProperties.CONFIG_PREFIX, name = "resolution-mode",
            havingValue = "cookie")
    CookieTenantResolver cookieTenantResolver(HttpTenantResolutionProperties httpTenantResolutionProperties) {
        return new CookieTenantResolver(httpTenantResolutionProperties.getCookie().getCookieName());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = HttpTenantResolutionProperties.CONFIG_PREFIX, name = "filter.enabled",
            havingValue = "true", matchIfMissing = true)
    static class HttpTenantFilterConfiguration {

        @Bean
        @ConditionalOnMissingBean
        TenantContextFilter tenantContextFilter(HttpRequestTenantResolver httpRequestTenantResolver,
                TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher,
                ApplicationEventPublisher eventPublisher, ObjectProvider<TenantVerifier> tenantVerifier,
                ObjectProvider<TenantObservationFilter> tenantObservationFilter) {
            return TenantContextFilter.builder()
                    .httpRequestTenantResolver(httpRequestTenantResolver)
                    .tenantContextIgnorePathMatcher(tenantContextIgnorePathMatcher)
                    .eventPublisher(eventPublisher)
                    .tenantVerifier(tenantVerifier.getIfAvailable())
                    .tenantObservationFilter(tenantObservationFilter.getIfAvailable())
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean
        TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher(
                HttpTenantResolutionProperties httpTenantResolutionProperties) {
            HashSet<String> ignorePathMatcher = new HashSet<String>(
                    httpTenantResolutionProperties.getFilter().getIgnorePaths());
            ignorePathMatcher.addAll(httpTenantResolutionProperties.getFilter().getAdditionalIgnorePaths());
            return new TenantContextIgnorePathMatcher(ignorePathMatcher);
        }
    }

}
