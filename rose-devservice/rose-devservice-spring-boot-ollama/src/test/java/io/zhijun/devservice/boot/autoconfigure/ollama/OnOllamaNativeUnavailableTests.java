package io.zhijun.devservice.boot.autoconfigure.ollama;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OnOllamaNativeUnavailable}.
 */
class OnOllamaNativeUnavailableTests {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfiguration.class);

    private final OnOllamaNativeUnavailable condition = new OnOllamaNativeUnavailable();

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Test
    void returnsFalseWhenNativeConnectionUnavailable() {
        assertThat(condition.isOllamaNativeConnection("http://127.0.0.1:1")).isFalse();
    }

    @Test
    void returnsFalseForInvalidUrl() {
        assertThat(condition.isOllamaNativeConnection("not-a-url")).isFalse();
    }

    @Test
    void returnsTrueWhenNativeConnectionRespondsWithOk() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        int port = server.getAddress().getPort();

        assertThat(condition.isOllamaNativeConnection("http://127.0.0.1:" + port))
                .isTrue();
    }

    @Test
    void matchesWhenIgnoreNativeServiceIsEnabled() {
        contextRunner
                .withPropertyValues("rose.dev.ollama.ignore-native-service=true")
                .run(context -> assertThat(context).hasBean("ollamaDevServiceMarker"));
    }

    @Test
    void matchesWhenNativeConnectionIsUnavailable() {
        contextRunner
                .withPropertyValues("rose.dev.ollama.base-url=http://127.0.0.1:1")
                .run(context -> assertThat(context).hasBean("ollamaDevServiceMarker"));
    }

    @Test
    void doesNotMatchWhenNativeConnectionIsAvailable() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        int port = server.getAddress().getPort();

        contextRunner
                .withPropertyValues("rose.dev.ollama.base-url=http://127.0.0.1:" + port)
                .run(context -> assertThat(context).doesNotHaveBean("ollamaDevServiceMarker"));
    }

    @Test
    void matchesUsingDefaultBaseUrlWhenPropertyIsNotSet() {
        Assumptions.assumeFalse(
                condition.isOllamaNativeConnection("http://localhost:11434"),
                "Local Ollama is running on the default port");

        contextRunner.run(context -> assertThat(context).hasBean("ollamaDevServiceMarker"));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        @ConditionalOnOllamaNativeUnavailable
        String ollamaDevServiceMarker() {
            return "ollama-dev-service";
        }
    }
}
