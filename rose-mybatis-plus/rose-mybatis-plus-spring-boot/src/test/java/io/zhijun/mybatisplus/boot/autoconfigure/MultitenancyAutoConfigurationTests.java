package io.zhijun.mybatisplus.boot.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.mybatisplus.core.multitenancy.TenantIdSupplier;
import io.zhijun.mybatisplus.core.multitenancy.TenantLineInnerInterceptorRegistrar;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MultitenancyAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisPlusAutoConfiguration.class));

    @Test
    void shouldRegisterTenantLineWhenMultitenancyOnClasspath() {
        contextRunner.withUserConfiguration(MybatisPlusInterceptorConfig.class).run(context -> {
            assertThat(context).hasSingleBean(TenantIdSupplier.class);
            assertThat(context).hasSingleBean(TenantLineInnerInterceptorRegistrar.class);

            MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
            assertThat(interceptor.getInterceptors()).hasSize(1);
            assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TenantLineInnerInterceptor.class);
        });
    }

    @Test
    void shouldDisableTenantLineWhenPropertyIsFalse() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.multitenancy.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TenantLineInnerInterceptorRegistrar.class);
                });
    }

    @Test
    void shouldUseCustomTenantIdSupplier() {
        contextRunner
                .withUserConfiguration(CustomTenantIdSupplierConfig.class, MybatisPlusInterceptorConfig.class)
                .run(context -> {
                    TenantIdSupplier supplier = context.getBean(TenantIdSupplier.class);
                    assertThat(supplier.getTenantId()).isEqualTo("custom");

                    try (TenantContext.Scope ignored = TenantContext.bind("ignored")) {
                        assertThat(supplier.getTenantId()).isEqualTo("custom");
                    }
                });
    }

    @Configuration
    static class MybatisPlusInterceptorConfig {
        @Bean
        public MybatisPlusInterceptor mybatisPlusInterceptor() {
            return new MybatisPlusInterceptor();
        }
    }

    @Configuration
    static class CustomTenantIdSupplierConfig {
        @Bean
        public TenantIdSupplier tenantIdSupplier() {
            return () -> "custom";
        }
    }
}
