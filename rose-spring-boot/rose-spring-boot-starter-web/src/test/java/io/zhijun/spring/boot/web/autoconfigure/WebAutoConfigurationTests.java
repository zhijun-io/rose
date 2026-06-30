package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.web.metadata.DefaultWebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistrar;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistry;
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
 * {@link WebAutoConfiguration} 自动装配单元测试
 */
class WebAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME = WebAutoConfiguration.class.getName();

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
    void shouldRegisterWebEndpointMappingRegistry() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(WebEndpointMappingRegistry.class));
    }

    @Test
    void shouldRegisterDefaultWebEndpointMappingRegistry() {
        webContextRunner().run(context ->
                assertThat(context).getBean(WebEndpointMappingRegistry.class)
                        .isInstanceOf(DefaultWebEndpointMappingRegistry.class));
    }

    @Test
    void shouldRegisterWebEndpointMappingRegistrar() {
        webContextRunner().run(context ->
                assertThat(context).hasSingleBean(WebEndpointMappingRegistrar.class));
    }

    // ====== 属性控制测试 ======

    @Test
    void shouldBackOffWhenDisabled() {
        webContextRunner()
                .withPropertyValues("rose.spring.boot.web.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(WebEndpointMappingRegistry.class);
                    assertThat(context).doesNotHaveBean(WebEndpointMappingRegistrar.class);
                });
    }

    // ====== 非 Web 环境回退 ======

    @Test
    void shouldBackOffInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(WebEndpointMappingRegistry.class);
                    assertThat(context).doesNotHaveBean(WebEndpointMappingRegistrar.class);
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
                .withConfiguration(AutoConfigurations.of(WebAutoConfiguration.class));
    }
}
