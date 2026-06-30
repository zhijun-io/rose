package io.zhijun.mybatisplus.spring.annotation;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.spring.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class EnableMyBatisPlusExtensionTests {

    @Test
    void shouldRegisterBeanPostProcessorAndApplyCustomizers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            MybatisPlusInterceptor interceptor =
                    context.getBean("mybatisPlusInterceptor", MybatisPlusInterceptor.class);

            assertThat(interceptor.getInterceptors()).hasSize(1);
            assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TestInnerInterceptor.class);
        }
    }

    @Test
    void shouldRegisterBeanPostProcessorOnlyOnce() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] names = context.getBeanNamesForType(MybatisPlusInterceptorCustomizerBeanPostProcessor.class);
            assertThat(names).hasSize(1);
        }
    }

    @Configuration
    @EnableMyBatisPlusExtension
    static class TestConfig {

        @Bean
        public MybatisPlusInterceptor mybatisPlusInterceptor() {
            return new MybatisPlusInterceptor();
        }

        @Bean
        public MybatisPlusInterceptorCustomizer testCustomizer() {
            return interceptor -> interceptor.addInnerInterceptor(new TestInnerInterceptor());
        }
    }

    static class TestInnerInterceptor implements InnerInterceptor {}
}
