package io.zhijun.spring.config.env.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JsonPropertySourceTests.Config.class)
class JsonPropertySourceTests {

    @Value("${app.name}")
    private String appName;

    @Value("${server.features.ssl}")
    private boolean sslEnabled;

    @Test
    void shouldLoadJsonPropertySourceAnnotation() {
        assertThat(appName).isEqualTo("json-app");
        assertThat(sslEnabled).isTrue();
    }

    @Configuration
    @JsonPropertySource({
            "classpath:test-json/simple.json",
            "classpath:test-json/nested.json"
    })
    static class Config {
    }
}
