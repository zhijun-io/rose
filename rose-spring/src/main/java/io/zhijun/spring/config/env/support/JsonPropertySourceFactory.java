package io.zhijun.spring.config.env.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zhijun.spring.config.env.ImmutableMapPropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link PropertySourceFactory} for loading JSON resources as flattened property sources.
 *
 * @since 0.1.0
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (Reader reader = resource.getReader()) {
            Map source = objectMapper.readValue(reader, LinkedHashMap.class);
            return new MapPropertySource(name, source);
        }
    }
}
