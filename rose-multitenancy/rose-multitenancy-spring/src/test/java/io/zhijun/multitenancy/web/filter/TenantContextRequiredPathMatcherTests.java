package io.zhijun.multitenancy.web.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantContextRequiredPathMatcher}.
 */
class TenantContextRequiredPathMatcherTests {

    @Test
    void whenIncludeDoesNotMatchThenTenantNotRequired() {
        TenantContextRequiredPathMatcher matcher = new TenantContextRequiredPathMatcher(
                Collections.singleton("/v1/**"), Collections.emptySet());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/ping");

        assertThat(matcher.requires(request)).isFalse();
    }

    @Test
    void whenIncludeMatchesAndExcludeDoesNotThenTenantRequired() {
        TenantContextRequiredPathMatcher matcher = new TenantContextRequiredPathMatcher(
                Collections.singleton("/v1/**"), Collections.singleton("/v1/login"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/v1/users");

        assertThat(matcher.requires(request)).isTrue();
    }

    @Test
    void whenExcludeMatchesThenTenantNotRequired() {
        TenantContextRequiredPathMatcher matcher = new TenantContextRequiredPathMatcher(
                Collections.singleton("/v1/**"), Collections.singleton("/v1/login"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/v1/login");

        assertThat(matcher.requires(request)).isFalse();
    }

}
