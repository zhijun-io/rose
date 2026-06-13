package io.zhijun.multitenancy.web.context.resolvers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

import io.zhijun.multitenancy.core.context.resolvers.TenantResolver;

/**
 * Strategy used to resolve the current tenant from an HTTP request.
 */
public interface HttpRequestTenantResolver extends TenantResolver<HttpServletRequest> {

    /**
     * Resolves a tenant identifier from an HTTP request.
     */
    @Override
    @Nullable
    String resolveTenantIdentifier(HttpServletRequest request);

}
