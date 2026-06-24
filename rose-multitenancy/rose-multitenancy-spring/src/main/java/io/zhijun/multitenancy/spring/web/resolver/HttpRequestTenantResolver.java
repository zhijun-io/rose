package io.zhijun.multitenancy.spring.web.resolver;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

import io.zhijun.multitenancy.core.context.TenantResolver;

/**
 * Strategy used to resolve the current multitenancy from an HTTP request.
 */
public interface HttpRequestTenantResolver extends TenantResolver<HttpServletRequest> {

    /**
     * Resolves a multitenancy identifier from an HTTP request.
     */
    @Override
    @Nullable
    String resolveTenantId(HttpServletRequest request);

}
