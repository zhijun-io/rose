package io.zhijun.multitenancy.spring.web.resolver;

import io.zhijun.multitenancy.core.MultitenancyDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Strategy used to resolve the current multitenancy from a header in an HTTP request.
 */

public final class HeaderTenantResolver implements HttpRequestTenantResolver {

    public static final String DEFAULT_HEADER_NAME = MultitenancyDefaults.DEFAULT_HTTP_HEADER_NAME;

    private final String tenantHeaderName;

    public HeaderTenantResolver() {
        this.tenantHeaderName = DEFAULT_HEADER_NAME;
    }

    public HeaderTenantResolver(String tenantHeaderName) {
        Assert.hasText(tenantHeaderName, "tenantHeaderName cannot be null or empty");
        this.tenantHeaderName = tenantHeaderName;
    }

    @Override
    @Nullable
    public String resolveTenantIdentifier(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        return request.getHeader(tenantHeaderName);
    }
}
