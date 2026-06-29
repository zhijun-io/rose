package io.zhijun.multitenancy.spring.web.resolver;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


import io.zhijun.multitenancy.core.MultitenancyDefaults;

/**
 * Strategy used to resolve the current multitenancy from a cookie in an HTTP request.
 */

public final class CookieTenantResolver implements HttpRequestTenantResolver {

    public static final String DEFAULT_COOKIE_NAME = MultitenancyDefaults.DEFAULT_HTTP_COOKIE_NAME;

    private final String tenantCookieName;

    public CookieTenantResolver() {
        this.tenantCookieName = DEFAULT_COOKIE_NAME;
    }

    public CookieTenantResolver(String tenantCookieName) {
        Assert.hasText(tenantCookieName, "tenantCookieName cannot be null or empty");
        this.tenantCookieName = tenantCookieName;
    }

    @Override
    @Nullable
    public String resolveTenantIdentifier(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(tenantCookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
