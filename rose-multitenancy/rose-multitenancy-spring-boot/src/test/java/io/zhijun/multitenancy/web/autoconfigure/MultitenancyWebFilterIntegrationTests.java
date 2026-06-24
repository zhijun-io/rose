package io.zhijun.multitenancy.web.autoconfigure;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Filter;

import io.zhijun.multitenancy.autoconfigure.web.MultitenancyWebAutoConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.zhijun.multitenancy.autoconfigure.core.MultitenancyCoreAutoConfiguration;
import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.multitenancy.web.filter.TenantContextFilter;
import io.zhijun.multitenancy.web.resolver.HeaderTenantResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for HTTP multitenancy resolution auto-configuration.
 */
class MultitenancyWebFilterIntegrationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MultitenancyCoreAutoConfiguration.class,
                    MultitenancyWebAutoConfiguration.class));

    @Test
    void tenantContextFilterBindsTenantFromHeader() throws Exception {
        AtomicReference<String> capturedTenant = new AtomicReference<String>();
        contextRunner.run(context -> {
            @SuppressWarnings("unchecked")
            FilterRegistrationBean<Filter> registration =
                    (FilterRegistrationBean<Filter>) context.getBean("tenantContextFilterRegistration");
            TenantContextFilter filter = (TenantContextFilter) registration.getFilter();

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            request.addHeader(HeaderTenantResolver.DEFAULT_HEADER_NAME, "acme");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain() {
                @Override
                public void doFilter(javax.servlet.ServletRequest servletRequest,
                        javax.servlet.ServletResponse servletResponse) {
                    capturedTenant.set(TenantContext.getTenantId());
                }
            };

            filter.doFilter(request, response, filterChain);

            assertThat(capturedTenant.get()).isEqualTo("acme");
            assertThat(TenantContext.getTenantId()).isNull();
        });
    }

}
