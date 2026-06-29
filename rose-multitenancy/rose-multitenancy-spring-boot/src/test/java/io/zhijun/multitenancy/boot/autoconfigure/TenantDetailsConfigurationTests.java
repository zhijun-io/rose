package io.zhijun.multitenancy.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.multitenancy.boot.autoconfigure.detail.TenantDetailsConfiguration;
import io.zhijun.multitenancy.core.detail.DefaultTenantVerifier;
import io.zhijun.multitenancy.core.detail.TenantDetailsService;
import io.zhijun.multitenancy.core.detail.TenantVerifier;

/**
 * Unit test for {@link TenantDetailsConfiguration}.
 */
class TenantDetailsConfigurationTests {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(TenantDetailsConfiguration.class));

    @Test
    void tenantDetailsServiceWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(TenantDetailsService.class);
        });
    }

    @Test
    void tenantDetailsServiceWhenProperties() {
        contextRunner
                .withPropertyValues("rose.multitenancy.details.source=properties")
                .run(context -> {
                    assertThat(context).hasSingleBean(TenantDetailsService.class);
                });
    }

    @Test
    void tenantVerifierWhenNoService() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TenantVerifier.class);
            assertThat(context).hasSingleBean(io.zhijun.multitenancy.core.detail.FormatTenantVerifier.class);
        });
    }

    @Test
    void tenantVerifierWhenService() {
        contextRunner
                .withPropertyValues("rose.multitenancy.details.source=properties")
                .run(context -> {
                    assertThat(context).hasSingleBean(TenantVerifier.class);
                    assertThat(context).hasSingleBean(DefaultTenantVerifier.class);
                });
    }
}
