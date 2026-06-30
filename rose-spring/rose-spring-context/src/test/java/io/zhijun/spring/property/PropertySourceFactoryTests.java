package io.zhijun.spring.propertysource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;

import java.nio.charset.StandardCharsets;

class PropertySourceFactoryTests {

    @Test
    void shouldLoadYamlAsFlatMap() throws Exception {
        YamlPropertySourceFactory factory = new YamlPropertySourceFactory();
        String yaml = "app:\n  name: rose\n  nested:\n    enabled: true\n";

        org.springframework.core.env.PropertySource<?> propertySource = factory.createPropertySource(
                "yaml", new EncodedResource(new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8))));

        assertThat(propertySource.getProperty("app.name")).isEqualTo("rose");
        assertThat(propertySource.getProperty("app.nested.enabled")).isEqualTo(true);
    }

    @Test
    void shouldLoadJsonAsFlatMap() throws Exception {
        JsonPropertySourceFactory factory = new JsonPropertySourceFactory();
        String json = "{\"app\":{\"name\":\"rose\",\"nested\":{\"enabled\":true}}}";

        org.springframework.core.env.PropertySource<?> propertySource = factory.createPropertySource(
                "json", new EncodedResource(new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8))));

        assertThat(propertySource.getProperty("app.name")).isEqualTo("rose");
        assertThat(propertySource.getProperty("app.nested.enabled")).isEqualTo(true);
    }
}
