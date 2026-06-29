package io.zhijun.multitenancy.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.multitenancy.core.context.FixedTenantResolver;
import io.zhijun.multitenancy.spring.cache.TenantKeyGenerator;
import io.zhijun.multitenancy.spring.logging.MdcTenantEventListener;

/**
 * Unit test for {@link MultitenancyCoreAutoConfiguration}.
 */
class MultitenancyCoreAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MultitenancyCoreAutoConfiguration.class));

    @Test
    void tenantKeyGenerator() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TenantKeyGenerator.class);
        });
    }

    @Test
    void mdcTenantEventListenerWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MdcTenantEventListener.class);
        });
    }

    @Test
    void mdcTenantEventListenerWhenDisabled() {
        contextRunner
                .withPropertyValues("rose.multitenancy.logging.mdc.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MdcTenantEventListener.class);
                });
    }

    @Test
    void fixedTenantResolverWhenDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(FixedTenantResolver.class);
        });
    }

    @Test
    void fixedTenantResolverWhenEnabled() {
        contextRunner
                .withPropertyValues("rose.multitenancy.resolution.fixed.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(FixedTenantResolver.class);
                });
    }
}
