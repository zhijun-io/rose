package io.zhijun.multitenancy.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit test for {@link CookieTenantResolver}.
 */
class CookieTenantResolverTests {

    @Test
    void whenNullCustomCookieThenThrow() {
        assertThatThrownBy(() -> new CookieTenantResolver(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantCookieName cannot be null or empty");
    }

    @Test
    void whenEmptyCustomCookieThenThrow() {
        assertThatThrownBy(() -> new CookieTenantResolver(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantCookieName cannot be null or empty");
    }

    @Test
    void whenDefaultCookieIsUsed() {
        String expectedTenantId = "default";
        CookieTenantResolver cookieTenantResolver = new CookieTenantResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieTenantResolver.DEFAULT_COOKIE_NAME, expectedTenantId));

        String actualTenantId = cookieTenantResolver.resolveTenantIdentifier(request);

        assertThat(actualTenantId).isEqualTo(expectedTenantId);
    }

    @Test
    void whenCustomCookieIsUsed() {
        String expectedTenantId = "default";
        String cookieName = "tenantIdentifier";
        CookieTenantResolver cookieTenantResolver = new CookieTenantResolver(cookieName);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(cookieName, expectedTenantId));

        String actualTenantId = cookieTenantResolver.resolveTenantIdentifier(request);

        assertThat(actualTenantId).isEqualTo(expectedTenantId);
    }

    @Test
    void whenNoCookiesPresentThenReturnNull() {
        CookieTenantResolver cookieTenantResolver = new CookieTenantResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String actualTenantId = cookieTenantResolver.resolveTenantIdentifier(request);

        assertThat(actualTenantId).isNull();
    }

    @Test
    void whenNullRequestThenThrow() {
        CookieTenantResolver cookieTenantResolver = new CookieTenantResolver();

        assertThatThrownBy(() -> cookieTenantResolver.resolveTenantIdentifier(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("request cannot be null");
    }
}
