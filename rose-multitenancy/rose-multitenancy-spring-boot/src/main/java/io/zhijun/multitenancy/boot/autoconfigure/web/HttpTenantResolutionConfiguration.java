package io.zhijun.multitenancy.boot.autoconfigure.web;

import java.util.HashSet;

import javax.servlet.DispatcherType;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.multitenancy.boot.autoconfigure.FixedTenantResolutionProperties;
import io.zhijun.multitenancy.core.context.FixedTenantResolver;
import io.zhijun.multitenancy.core.detail.TenantVerifier;
import io.zhijun.multitenancy.spring.web.filter.TenantContextFilter;
import io.zhijun.multitenancy.spring.web.filter.TenantContextIgnorePathMatcher;
import io.zhijun.multitenancy.spring.web.filter.TenantContextMissingTenantHandler;
import io.zhijun.multitenancy.spring.web.filter.TenantContextRequiredPathMatcher;
import io.zhijun.multitenancy.spring.web.resolver.CookieTenantResolver;
import io.zhijun.multitenancy.spring.web.resolver.HeaderTenantResolver;
import io.zhijun.multitenancy.spring.web.resolver.HttpRequestTenantResolver;

/**
 * Configuration for HTTP multitenancy resolution.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HttpTenantResolutionProperties.class)
@ConditionalOnProperty(
        prefix = HttpTenantResolutionProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public final class HttpTenantResolutionConfiguration {

    @Bean
    @ConditionalOnBean(FixedTenantResolver.class)
    @ConditionalOnProperty(
            prefix = FixedTenantResolutionProperties.CONFIG_PREFIX,
            name = "enabled",
            havingValue = "true")
    HttpRequestTenantResolver fixedHttpRequestTenantResolver(final FixedTenantResolver fixedTenantResolver) {
        return request -> fixedTenantResolver.resolveTenantIdentifier(request);
    }

    @Bean
    @ConditionalOnMissingBean(HttpRequestTenantResolver.class)
    @ConditionalOnProperty(
            prefix = HttpTenantResolutionProperties.CONFIG_PREFIX,
            name = "resolution-mode",
            havingValue = "header",
            matchIfMissing = true)
    HeaderTenantResolver headerTenantResolver(HttpTenantResolutionProperties httpTenantResolutionProperties) {
        return new HeaderTenantResolver(
                httpTenantResolutionProperties.getHeader().getHeaderName());
    }

    @Bean
    @ConditionalOnMissingBean(HttpRequestTenantResolver.class)
    @ConditionalOnProperty(
            prefix = HttpTenantResolutionProperties.CONFIG_PREFIX,
            name = "resolution-mode",
            havingValue = "cookie")
    CookieTenantResolver cookieTenantResolver(HttpTenantResolutionProperties httpTenantResolutionProperties) {
        return new CookieTenantResolver(
                httpTenantResolutionProperties.getCookie().getCookieName());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = HttpTenantResolutionProperties.CONFIG_PREFIX,
            name = "filter.enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class HttpTenantFilterConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "tenantContextFilterRegistration")
        FilterRegistrationBean<TenantContextFilter> tenantContextFilterRegistration(
                HttpRequestTenantResolver httpRequestTenantResolver,
                TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher,
                ObjectProvider<TenantContextRequiredPathMatcher> tenantContextRequiredPathMatcher,
                ApplicationEventPublisher eventPublisher,
                ObjectProvider<TenantVerifier> tenantVerifier,
                ObjectProvider<TenantContextMissingTenantHandler> missingTenantHandler) {
            TenantContextFilter filter = TenantContextFilter.builder()
                    .httpRequestTenantResolver(httpRequestTenantResolver)
                    .tenantContextIgnorePathMatcher(tenantContextIgnorePathMatcher)
                    .tenantContextRequiredPathMatcher(tenantContextRequiredPathMatcher.getIfAvailable())
                    .eventPublisher(eventPublisher)
                    .tenantVerifier(tenantVerifier.getIfAvailable())
                    .missingTenantHandler(missingTenantHandler.getIfAvailable())

                    .build();
            FilterRegistrationBean<TenantContextFilter> registration =
                    new FilterRegistrationBean<TenantContextFilter>();
            registration.setFilter(filter);
            registration.addUrlPatterns("/*");
            registration.setDispatcherTypes(DispatcherType.REQUEST);
            registration.setName("tenantContextFilter");
            registration.setOrder(TenantContextFilter.FILTER_ORDER);
            return registration;
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

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                prefix = HttpTenantResolutionProperties.CONFIG_PREFIX + ".filter",
                name = "required-include-paths")
        TenantContextRequiredPathMatcher tenantContextRequiredPathMatcher(
                HttpTenantResolutionProperties httpTenantResolutionProperties) {
            return new TenantContextRequiredPathMatcher(
                    httpTenantResolutionProperties.getFilter().getRequiredIncludePaths(),
                    httpTenantResolutionProperties.getFilter().getRequiredExcludePaths());
        }
    }
}
