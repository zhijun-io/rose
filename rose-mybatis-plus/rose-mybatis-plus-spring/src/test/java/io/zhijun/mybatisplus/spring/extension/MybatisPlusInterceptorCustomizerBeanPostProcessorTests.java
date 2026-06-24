package io.zhijun.mybatisplus.spring.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

class MybatisPlusInterceptorCustomizerBeanPostProcessorTests {

    @Test
    void shouldApplyCustomizersToMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        MybatisPlusInterceptorCustomizer customizer = i ->
                i.addInnerInterceptor(new TestInnerInterceptor());

        MybatisPlusInterceptorCustomizerBeanPostProcessor bpp =
                new MybatisPlusInterceptorCustomizerBeanPostProcessor(Collections.singletonList(customizer));

        Object result = bpp.postProcessAfterInitialization(interceptor, "mybatisPlusInterceptor");

        assertThat(result).isSameAs(interceptor);
        assertThat(interceptor.getInterceptors()).hasSize(1);
        assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TestInnerInterceptor.class);
    }

    @Test
    void shouldIgnoreNonInterceptorBeans() {
        MybatisPlusInterceptorCustomizer customizer = i ->
                i.addInnerInterceptor(new TestInnerInterceptor());
        MybatisPlusInterceptorCustomizerBeanPostProcessor bpp =
                new MybatisPlusInterceptorCustomizerBeanPostProcessor(Collections.singletonList(customizer));

        Object result = bpp.postProcessAfterInitialization("not-an-interceptor", "otherBean");

        assertThat(result).isEqualTo("not-an-interceptor");
    }

    static class TestInnerInterceptor implements InnerInterceptor {
    }

}
