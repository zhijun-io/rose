package io.zhijun.spring.config.env;

import io.zhijun.spring.config.env.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import static org.assertj.core.api.Assertions.assertThat;

class YamlPropertySourceFactoryTests {

    private final YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

    @Test
    void shouldCreatePropertySourceFromYaml() throws Exception {
        EncodedResource resource = new EncodedResource(new ClassPathResource("test-yaml/simple.yml"));

        PropertySource<?> propertySource = factory.createPropertySource("testYaml", resource);

        assertThat(propertySource.getName()).isEqualTo("testYaml");
        assertThat(propertySource.getProperty("app.name")).isEqualTo("test-app");
        assertThat(propertySource.getProperty("server.port")).isEqualTo(8080);
    }
}
