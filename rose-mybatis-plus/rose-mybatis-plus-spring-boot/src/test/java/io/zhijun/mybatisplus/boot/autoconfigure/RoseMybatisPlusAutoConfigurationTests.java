package io.zhijun.mybatisplus.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.mybatisplus.core.crypto.EncryptionKeyResolver;
import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.spring.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;

class RoseMybatisPlusAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoseMybatisPlusAutoConfiguration.class));

    @Test
    void shouldRegisterCustomizerBeanPostProcessor() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MybatisPlusInterceptorCustomizerBeanPostProcessor.class);
        });
    }

    @Test
    void shouldApplySpringBeanCustomizersToInterceptor() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.multitenancy.enabled=false")
                .withUserConfiguration(CustomizerConfig.class, MybatisPlusInterceptorConfig.class)
                .run(context -> {
                    MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
                    assertThat(interceptor.getInterceptors()).hasSize(1);
                    assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TestInnerInterceptor.class);
                });
    }

    @Test
    void shouldDisableWhenPropertyIsFalse() {
        contextRunner.withPropertyValues("rose.mybatis-plus.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(RoseMybatisPlusAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(MybatisPlusInterceptorCustomizerBeanPostProcessor.class);
        });
    }

    @Test
    void shouldResolveDefaultSecretFromPassword() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.encryptor.password=mySecret")
                .run(context -> {
                    EncryptionKeyResolver resolver = context.getBean(EncryptionKeyResolver.class);
                    assertThat(resolver.resolve("default")).isEqualTo("mySecret");
                });
    }

    @Test
    void shouldThrowForNonDefaultSecretRef() {
        contextRunner
                .withPropertyValues("rose.mybatis-plus.encryptor.password=mySecret")
                .run(context -> {
                    EncryptionKeyResolver resolver = context.getBean(EncryptionKeyResolver.class);
                    assertThatThrownBy(() -> resolver.resolve("custom"))
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("custom");
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
    static class CustomizerConfig {
        @Bean
        public MybatisPlusInterceptorCustomizer testCustomizer() {
            return interceptor -> interceptor.addInnerInterceptor(new TestInnerInterceptor());
        }
    }

    static class TestInnerInterceptor implements InnerInterceptor {}
}
