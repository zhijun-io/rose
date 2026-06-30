package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.webmvc.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.zhijun.spring.webmvc.ContentCachingFilter;
import io.zhijun.spring.webmvc.ExclusiveViewResolverApplicationListener;
import io.zhijun.spring.webmvc.ReversedProxyHandlerMapping;
import io.zhijun.spring.webmvc.annotation.WebMvcExtensionConfiguration;
import io.zhijun.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.zhijun.spring.webmvc.interceptor.LoggingMethodHandlerInterceptor;
import io.zhijun.spring.webmvc.interceptor.LoggingPageRenderContextHandlerInterceptor;
import io.zhijun.spring.webmvc.method.InterceptingHandlerMethodProcessor;
import io.zhijun.spring.webmvc.method.LoggingHandlerMethodArgumentResolverAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebMvcAutoConfiguration} 自动装配单元测试
 */
class WebMvcAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME = WebMvcAutoConfiguration.class.getName();

    // ====== 文件注册验证 ======

    @Test
    void shouldRegisterAutoConfigurationInImportsFile() throws IOException {
        assertThat(resource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .contains(AUTO_CONFIGURATION_NAME);
    }

    @Test
    void shouldRegisterAutoConfigurationInSpringFactories() throws IOException {
        assertThat(resource("META-INF/spring.factories"))
                .contains(AUTO_CONFIGURATION_NAME);
    }

    // ====== Bean 注册验证（Servlet Web 环境） ======

    @Test
    void shouldRegisterContentCachingFilter() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(ContentCachingFilter.class));
    }

    @Test
    void shouldRegisterContentNegotiationManagerWebMvcConfigurer() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(ConfigurableContentNegotiationManagerWebMvcConfigurer.class));
    }

    @Test
    void shouldRegisterReversedProxyHandlerMapping() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(ReversedProxyHandlerMapping.class));
    }

    @Test
    void shouldRegisterWebMvcExtensionConfiguration() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(WebMvcExtensionConfiguration.class));
    }

    @Test
    void shouldRegisterInterceptingHandlerMethodProcessor() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(InterceptingHandlerMethodProcessor.class));
    }

    @Test
    void shouldRegisterLazyCompositeHandlerInterceptor() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(LazyCompositeHandlerInterceptor.class));
    }

    @Test
    void shouldRegisterLoggingMethodHandlerInterceptor() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(LoggingMethodHandlerInterceptor.class));
    }

    @Test
    void shouldRegisterLoggingPageRenderContextHandlerInterceptor() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(LoggingPageRenderContextHandlerInterceptor.class));
    }

    @Test
    void shouldRegisterLoggingHandlerMethodArgumentResolverAdvice() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(LoggingHandlerMethodArgumentResolverAdvice.class));
    }

    // ====== 条件属性控制测试 ======

    @Test
    void shouldDisableContentCachingFilterViaProperty() {
        webContextRunner()
                .withPropertyValues("rose.spring.boot.webmvc.filter.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(ContentCachingFilter.class));
    }

    @Test
    void shouldDisableContentNegotiationViaProperty() {
        webContextRunner()
                .withPropertyValues("rose.spring.boot.webmvc.content-negotiation.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(ConfigurableContentNegotiationManagerWebMvcConfigurer.class));
    }

    @Test
    void shouldDisableLoggingViaProperty() {
        webContextRunner()
                .withPropertyValues("rose.spring.boot.webmvc.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LoggingMethodHandlerInterceptor.class);
                    assertThat(context).doesNotHaveBean(LoggingPageRenderContextHandlerInterceptor.class);
                    assertThat(context).doesNotHaveBean(LoggingHandlerMethodArgumentResolverAdvice.class);
                });
    }

    @Test
    void shouldRegisterExclusiveViewResolverListenerOnlyWhenPropertySet() {
        // 未设置属性时不应注册
        webContextRunner().run(context ->
                assertThat(context).doesNotHaveBean(ExclusiveViewResolverApplicationListener.class));

        // 设置 bean 名称后应注册
        webContextRunner()
                .withPropertyValues("rose.spring.webmvc.view-resolver.exclusive-bean-name=testViewResolver")
                .run(context ->
                        assertThat(context).hasSingleBean(ExclusiveViewResolverApplicationListener.class));
    }

    // ====== 非 Web 环境回退 ======

    @Test
    void shouldBackOffInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ContentCachingFilter.class);
                    assertThat(context).doesNotHaveBean(ConfigurableContentNegotiationManagerWebMvcConfigurer.class);
                    assertThat(context).doesNotHaveBean(ReversedProxyHandlerMapping.class);
                });
    }

    // ====== 工具方法 ======

    private String resource(String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        assertThat(inputStream).as(name).isNotNull();
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }

    private WebApplicationContextRunner webContextRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class));
    }
}
