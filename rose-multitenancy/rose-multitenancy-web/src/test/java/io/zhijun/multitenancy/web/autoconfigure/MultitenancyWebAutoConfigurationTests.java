package io.zhijun.multitenancy.web.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import io.zhijun.multitenancy.core.autoconfigure.MultitenancyCoreAutoConfiguration;
import io.zhijun.multitenancy.web.context.filters.TenantContextFilter;
import io.zhijun.multitenancy.web.context.filters.TenantContextIgnorePathMatcher;
import io.zhijun.multitenancy.web.context.resolvers.CookieTenantResolver;
import io.zhijun.multitenancy.web.context.resolvers.HeaderTenantResolver;
import io.zhijun.multitenancy.web.context.resolvers.HttpRequestTenantResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MultitenancyWebAutoConfiguration}.
 */
class MultitenancyWebAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MultitenancyCoreAutoConfiguration.class, MultitenancyWebAutoConfiguration.class));

    @Test
    void whenNoServletContextThenBackOff() {
        ApplicationContextRunner nonServletContextRunner = new ApplicationContextRunner().withConfiguration(
                AutoConfigurations.of(MultitenancyCoreAutoConfiguration.class, MultitenancyWebAutoConfiguration.class));

        nonServletContextRunner
                .run(context -> assertThat(context).doesNotHaveBean(HttpTenantResolutionConfiguration.class));
    }

    @Test
    void httpTenantResolutionDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HttpTenantResolutionConfiguration.class);
        });
    }

    @Test
    void httpTenantResolutionDisabled() {
        contextRunner.withPropertyValues("rose.multitenancy.resolution.http.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(HttpTenantResolutionConfiguration.class));
    }

    @Test
    void httpRequestTenantResolverDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HeaderTenantResolver.class);
        });
    }

    @Test
    void httpRequestTenantResolverCookie() {
        contextRunner.withPropertyValues("rose.multitenancy.resolution.http.resolution-mode=cookie").run(context -> {
            assertThat(context).hasSingleBean(CookieTenantResolver.class);
        });
    }

    @Test
    void httpRequestTenantResolverFixed() {
        contextRunner
                .withPropertyValues("rose.multitenancy.resolution.fixed.enabled=true",
                        "rose.multitenancy.resolution.fixed.tenant-identifier=myTenant")
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpRequestTenantResolver.class);
                    HttpRequestTenantResolver httpRequestTenantResolver = context.getBean(HttpRequestTenantResolver.class);
                    assertThat(httpRequestTenantResolver.resolveTenantIdentifier(new MockHttpServletRequest()))
                            .isEqualTo("myTenant");
                });
    }

    @Test
    void tenantContextFilterDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TenantContextFilter.class);
        });
    }

    @Test
    void tenantContextIgnorePathMatcher() {
        contextRunner
                .withPropertyValues("rose.multitenancy.resolution.http.filter.ignore-paths=/actuator/**,/status")
                .run(context -> {
                    assertThat(context).hasSingleBean(TenantContextIgnorePathMatcher.class);
                    TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher = context.getBean(TenantContextIgnorePathMatcher.class);
                    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
                    mockRequest.setRequestURI("/actuator/prometheus");
                    assertThat(tenantContextIgnorePathMatcher.matches(mockRequest)).isTrue();
                });
    }

    @Test
    void tenantContextFilterDisabled() {
        contextRunner.withPropertyValues("rose.multitenancy.resolution.http.filter.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(TenantContextFilter.class));
    }

    @Test
    void tenantContextIgnorePathMatcherDisabled() {
        contextRunner.withPropertyValues("rose.multitenancy.resolution.http.filter.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(TenantContextIgnorePathMatcher.class));
    }

}
