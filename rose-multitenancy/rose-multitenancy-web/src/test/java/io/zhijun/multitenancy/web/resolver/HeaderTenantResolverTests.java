package io.zhijun.multitenancy.web.resolver;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link HeaderTenantResolver}.
 */
class HeaderTenantResolverTests {

    @Test
    void whenNullCustomHeaderThenThrow() {
        assertThatThrownBy(() -> new HeaderTenantResolver(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tenantHeaderName cannot be null or empty");
    }

    @Test
    void whenEmptyCustomHeaderThenThrow() {
        assertThatThrownBy(() -> new HeaderTenantResolver("")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tenantHeaderName cannot be null or empty");
    }

    @Test
    void whenDefaultHeaderIsUsed() {
        String expectedTenantId = "default";
        HeaderTenantResolver headerTenantResolver = new HeaderTenantResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderTenantResolver.DEFAULT_HEADER_NAME, expectedTenantId);

        String actualTenantId = headerTenantResolver.resolveTenantId(request);

        assertThat(actualTenantId).isEqualTo(expectedTenantId);
    }

    @Test
    void whenCustomHeaderIsUsed() {
        String expectedTenantId = "default";
        String headerName = "tenantIdentifier";
        HeaderTenantResolver headerTenantResolver = new HeaderTenantResolver(headerName);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerName, expectedTenantId);

        String actualTenantId = headerTenantResolver.resolveTenantId(request);

        assertThat(actualTenantId).isEqualTo(expectedTenantId);
    }

    @Test
    void whenHeaderMissingThenReturnNull() {
        HeaderTenantResolver headerTenantResolver = new HeaderTenantResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String actualTenantId = headerTenantResolver.resolveTenantId(request);

        assertThat(actualTenantId).isNull();
    }

    @Test
    void whenNullRequestThenThrow() {
        HeaderTenantResolver headerTenantResolver = new HeaderTenantResolver();

        assertThatThrownBy(() -> headerTenantResolver.resolveTenantId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("request cannot be null");
    }

}
