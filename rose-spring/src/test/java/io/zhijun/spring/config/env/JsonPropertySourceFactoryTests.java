package io.zhijun.spring.config.env;

import io.zhijun.spring.config.env.support.JsonPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPropertySourceFactoryTests {

    private final JsonPropertySourceFactory factory = new JsonPropertySourceFactory();

    @Test
    void shouldCreatePropertySourceFromJson() throws Exception {
        EncodedResource resource = new EncodedResource(new ClassPathResource("test-json/simple.json"));

        PropertySource<?> propertySource = factory.createPropertySource("testJson", resource);

        assertThat(propertySource.getName()).isEqualTo("testJson");
        assertThat(propertySource.getProperty("app.name")).isEqualTo("json-app");
        assertThat(propertySource.getProperty("app.version")).isEqualTo("2.0.0");
    }

    @Test
    void shouldFlattenNestedJsonProperties() throws Exception {
        EncodedResource resource = new EncodedResource(new ClassPathResource("test-json/nested.json"));

        PropertySource<?> propertySource = factory.createPropertySource("nestedJson", resource);

        assertThat(propertySource.getProperty("server.port")).isEqualTo(9090);
        assertThat(propertySource.getProperty("server.features.ssl")).isEqualTo(true);
        assertThat(propertySource.getProperty("server.features.cors")).isEqualTo(false);
    }
}
