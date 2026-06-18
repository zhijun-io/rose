package io.zhijun.spring.config.env.support;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * PropertySourceFactory for YAML.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try (Reader reader = resource.getReader()) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map)) {
                return new MapPropertySource(name, java.util.Collections.<String, Object>emptyMap());
            }
            Map<String, Object> flattened = PropertySourceMaps.flatten((Map<?, ?>) loaded);
            return new MapPropertySource(name, flattened);
        }
    }
}
