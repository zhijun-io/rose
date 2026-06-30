package io.zhijun.spring.config.property;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import static org.assertj.core.api.Assertions.assertThat;

class YamlPropertySourceFactoryTests {

    private final YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

    @Test
    void shouldCreatePropertySourceFromYaml() throws Exception {
        String yaml = "foo: bar\nnested:\n  key: value\n";
        EncodedResource resource = new EncodedResource(new ByteArrayResource(yaml.getBytes()));
        PropertySource<?> ps = factory.createPropertySource("test", resource);
        assertThat(ps).isNotNull();
        assertThat(ps.getProperty("foo")).isEqualTo("bar");
        assertThat(ps.getProperty("nested.key")).isEqualTo("value");
    }

    @Test
    void shouldCreateEmptyPropertySourceForNonMapYaml() throws Exception {
        String yaml = "[1, 2, 3]";
        EncodedResource resource = new EncodedResource(new ByteArrayResource(yaml.getBytes()));
        PropertySource<?> ps = factory.createPropertySource("test", resource);
        assertThat(ps).isNotNull();
        assertThat(ps.getProperty("anything")).isNull();
    }
}
