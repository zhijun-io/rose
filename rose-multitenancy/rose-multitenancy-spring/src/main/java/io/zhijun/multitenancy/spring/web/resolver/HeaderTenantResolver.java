package io.zhijun.multitenancy.spring.web.resolver;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.zhijun.annotation.Incubating;

/**
 * Strategy used to resolve the current multitenancy from a header in an HTTP request.
 */
@Incubating
public final class HeaderTenantResolver implements HttpRequestTenantResolver {

    public static final String DEFAULT_HEADER_NAME = "X-TenantId";

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
