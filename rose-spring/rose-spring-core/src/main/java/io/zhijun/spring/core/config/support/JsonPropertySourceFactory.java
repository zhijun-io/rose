package io.zhijun.spring.core.config.support;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * PropertySourceFactory for JSON.
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (Reader reader = resource.getReader()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> loaded = objectMapper.readValue(reader, LinkedHashMap.class);
            return new MapPropertySource(name, PropertySourceMaps.flatten(loaded));
        }
    }
}
