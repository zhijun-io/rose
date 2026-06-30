package io.zhijun.multitenancy.spring.web.resolver;

import io.zhijun.multitenancy.core.context.TenantResolver;
import org.jspecify.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * Strategy used to resolve the current multitenancy from an HTTP request.
 */
@FunctionalInterface
public interface HttpRequestTenantResolver extends TenantResolver<HttpServletRequest> {

    /**
     * Resolves a multitenancy identifier from an HTTP request.
     */
    @Override
    @Nullable
    String resolveTenantIdentifier(HttpServletRequest request);
}
