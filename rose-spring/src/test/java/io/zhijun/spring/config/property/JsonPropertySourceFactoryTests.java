package io.zhijun.spring.config.property;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonPropertySourceFactoryTests {

    private final JsonPropertySourceFactory factory = new JsonPropertySourceFactory();

    @Test
    void shouldCreatePropertySourceFromJson() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-json/simple.json");
        PropertySource<?> ps = factory.createPropertySource("json-source", new EncodedResource(resource));

        assertThat(ps.getName()).isEqualTo("json-source");
        assertThat(ps.getProperty("app.name")).isEqualTo("json-app");
        assertThat(ps.getProperty("app.version")).isEqualTo("2.0.0");
    }

    @Test
    void shouldFlattenNestedJson() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-json/nested.json");
        PropertySource<?> ps = factory.createPropertySource("nested", new EncodedResource(resource));

        assertThat(ps.getProperty("server.port")).isEqualTo(9090);
        assertThat(ps.getProperty("server.features.ssl")).isEqualTo(true);
        assertThat(ps.getProperty("server.features.cors")).isEqualTo(false);
    }

    @Test
    void shouldThrowForInvalidJson() {
        ClassPathResource resource = new ClassPathResource("test-json/nested.json");
        // we can test that it throws on a non-JSON resource
        assertThatThrownBy(() -> {
            ClassPathResource invalid = new ClassPathResource("test-yaml/simple.yml");
            factory.createPropertySource("invalid", new EncodedResource(invalid));
        }).isInstanceOf(Exception.class);
    }
}
