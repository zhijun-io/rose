package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.web.ApplicationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME = ErrorResponseAutoConfiguration.class.getName();

    @Test
    void shouldRegisterApplicationExceptionHandlerInServletWebApplication() {
        webContextRunner().run(context -> assertThat(context).hasSingleBean(ApplicationExceptionHandler.class));
    }

    @Test
    void shouldBackOffInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorResponseAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(ApplicationExceptionHandler.class));
    }

    @Test
    void shouldBackOffWhenUserDefinesApplicationExceptionHandler() {
        webContextRunner().withUserConfiguration(CustomHandlerConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(ApplicationExceptionHandler.class);
            assertThat(context.getBean(ApplicationExceptionHandler.class))
                    .isSameAs(context.getBean("customApplicationExceptionHandler"));
        });
    }

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

    private String resource(String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        assertThat(inputStream).as(name).isNotNull();
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
        finally {
            inputStream.close();
        }
    }

    private WebApplicationContextRunner webContextRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorResponseAutoConfiguration.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomHandlerConfiguration {

        @Bean
        ApplicationExceptionHandler customApplicationExceptionHandler() {
            return new ApplicationExceptionHandler();
        }
    }
}
