package io.zhijun.spring.core.propertysource.support;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try (Reader reader = resource.getReader()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> loaded = OBJECT_MAPPER.readValue(reader, LinkedHashMap.class);
            return new MapPropertySource(name, PropertySourceMaps.flatten(loaded));
        }
    }
}
