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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try (Reader reader = resource.getReader()) {
            JsonNode root = OBJECT_MAPPER.readTree(reader);
            Map<String, Object> properties = new LinkedHashMap<String, Object>();
            flatten("", root, properties);
            String propertySourceName = (name != null && !name.isEmpty()) ? name : resource.getResource().getDescription();
            return new ImmutableMapPropertySource(propertySourceName, properties);
        }
    }

    private static void flatten(String path, JsonNode node, Map<String, Object> properties) {
        if (node == null || node.isNull()) {
            if (!path.isEmpty()) {
                properties.put(path, null);
            }
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String childPath = path.isEmpty() ? field.getKey() : path + "." + field.getKey();
                flatten(childPath, field.getValue(), properties);
            }
            return;
        }
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flatten(path + "[" + i + "]", node.get(i), properties);
            }
            return;
        }
        if (node.isNumber()) {
            properties.put(path, node.numberValue());
            return;
        }
        if (node.isBoolean()) {
            properties.put(path, node.booleanValue());
            return;
        }
        properties.put(path, node.asText());
    }
}
