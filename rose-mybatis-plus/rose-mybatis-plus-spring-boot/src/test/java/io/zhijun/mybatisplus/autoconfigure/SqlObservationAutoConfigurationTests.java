package io.zhijun.mybatisplus.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.zhijun.mybatisplus.observation.SqlObservationInterceptor;

class SqlObservationAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisPlusAutoConfiguration.class));

    @Test
    void shouldRegisterInterceptorWhenMeterRegistryPresent() {
        contextRunner.withUserConfiguration(MeterRegistryConfig.class).run(context -> {
            assertThat(context).hasSingleBean(SqlObservationInterceptor.class);
            SqlObservationInterceptor interceptor = context.getBean(SqlObservationInterceptor.class);
            assertThat(interceptor).isNotNull();
        });
    }

    @Test
    void shouldNotRegisterInterceptorWithoutObservationBackend() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(SqlObservationInterceptor.class));
    }

    @Test
    void shouldDisableWhenPropertyIsFalse() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.observation.enabled=false")
                .withUserConfiguration(MeterRegistryConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(SqlObservationInterceptor.class));
    }

    @Test
    void shouldDisableWhenMybatisPlusDisabled() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.enabled=false")
                .withUserConfiguration(MeterRegistryConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(SqlObservationInterceptor.class));
    }

    @Configuration
    static class MeterRegistryConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

}
