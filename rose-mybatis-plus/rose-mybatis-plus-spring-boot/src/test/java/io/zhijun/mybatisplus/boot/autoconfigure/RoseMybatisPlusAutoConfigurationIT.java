package io.zhijun.mybatisplus.boot.autoconfigure;

import io.zhijun.mybatisplus.spring.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RoseMybatisPlusAutoConfiguration}.
 */
class RoseMybatisPlusAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisPlusAutoConfiguration.class));

    @Test
    void shouldStartWithDefaultProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MybatisPlusInterceptorCustomizerBeanPostProcessor.class);
            assertThat(context).hasSingleBean(RoseMybatisPlusAutoConfiguration.class);
        });
    }

    @Test
    void shouldHonorDisableProperty() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RoseMybatisPlusAutoConfiguration.class));
    }
}
