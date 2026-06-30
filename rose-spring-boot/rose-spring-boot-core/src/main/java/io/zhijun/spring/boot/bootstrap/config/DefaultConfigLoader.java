package io.zhijun.spring.boot.bootstrap.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.zhijun.spring.boot.constants.PropertyConstants;
 import io.zhijun.spring.config.property.PropertySourceMaps;

/**
 * Loads and merges {@code config/default/*} configuration resources from the classpath.
 */
final class DefaultConfigLoader {

    private static final String AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME =
            PropertyConstants.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME;

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigLoader.class);

    private final ResourcePatternResolver resourcePatternResolver;

    DefaultConfigLoader() {
        this(new PathMatchingResourcePatternResolver());
    }

    DefaultConfigLoader(ResourcePatternResolver resourcePatternResolver) {
        Assert.notNull(resourcePatternResolver, "resourcePatternResolver cannot be null");
        this.resourcePatternResolver = resourcePatternResolver;
    }

    Map<String, Object> load(String... locationPatterns) {
        Assert.notNull(locationPatterns, "locationPatterns cannot be null");
        List<Resource> resources = resolveResources(locationPatterns);
        Map<String, Object> merged = new HashMap<String, Object>();
        for (Resource resource : resources) {
            mergeResource(merged, resource);
        }
        return merged;
    }

    private List<Resource> resolveResources(String[] locationPatterns) {
        List<Resource> resources = new ArrayList<Resource>();
        for (String pattern : locationPatterns) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            try {
                Collections.addAll(resources, resourcePatternResolver.getResources(pattern.trim()));
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to resolve default config location pattern: " + pattern, ex);
            }
        }
        Collections.sort(resources, new Comparator<Resource>() {
            @Override
            public int compare(Resource left, Resource right) {
                return left.getDescription().compareTo(right.getDescription());
            }
        });
        return resources;
    }

    private static void mergeResource(Map<String, Object> merged, Resource resource) {
        if (resource == null || !resource.exists()) {
            return;
        }
        String filename = resource.getFilename();
        if (filename != null && isYamlFile(filename)) {
            mergeYaml(merged, resource);
            return;
        }
        mergeProperties(merged, resource);
    }

    private static boolean isYamlFile(String filename) {
        return filename.endsWith(".yml") || filename.endsWith(".yaml");
    }

    private static void mergeProperties(Map<String, Object> merged, Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            for (String name : properties.stringPropertyNames()) {
                putMerged(merged, resource, name, properties.getProperty(name));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load default config from " + resource.getDescription(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static void mergeYaml(Map<String, Object> merged, Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            boolean mergedDocument = false;
            for (Object document : yaml.loadAll(reader)) {
                if (document == null) {
                    continue;
                }
                if (!(document instanceof Map)) {
                    logger.warn("Skipping non-map YAML document in {}", resource.getDescription());
                    continue;
                }
                Map<String, Object> flattened = PropertySourceMaps.flatten((Map<?, ?>) document);
                for (Map.Entry<String, Object> entry : flattened.entrySet()) {
                    putMerged(
                            merged,
                            resource,
                            entry.getKey(),
                            PropertySourceMaps.normalizePropertyValue(entry.getValue()));
                }
                mergedDocument = true;
            }
            if (!mergedDocument && logger.isDebugEnabled()) {
                logger.debug("No YAML documents loaded from {}", resource.getDescription());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load default config from " + resource.getDescription(), ex);
        }
    }

    private static void putMerged(Map<String, Object> merged, Resource resource, String key, Object value) {
        Object previous = merged.get(key);
        if (previous != null && AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME.equals(key)) {
            Object accumulated = accumulateExcludeValues(previous, value);
            merged.put(key, accumulated);
            if (!valuesEqual(previous, accumulated) && logger.isDebugEnabled()) {
                logger.debug(
                        "Rose default config key '{}' accumulated in {} (previous: {}, added: {}, result: {})",
                        key,
                        resource.getDescription(),
                        previous,
                        value,
                        accumulated);
            }
            return;
        }
        previous = merged.put(key, value);
        if (previous != null && !valuesEqual(previous, value) && logger.isDebugEnabled()) {
            logger.debug(
                    "Rose default config key '{}' overridden in {} (previous: {}, new: {})",
                    key,
                    resource.getDescription(),
                    previous,
                    value);
        }
    }

    private static Object accumulateExcludeValues(Object previous, Object value) {
        if (!(previous instanceof String) || !(value instanceof String)) {
            return value;
        }
        String previousValue = (String) previous;
        String valueToAdd = (String) value;
        if (!StringUtils.hasText(previousValue)) {
            return valueToAdd;
        }
        if (!StringUtils.hasText(valueToAdd)) {
            return previousValue;
        }
        return previousValue + "," + valueToAdd;
    }

    private static boolean valuesEqual(Object left, Object right) {
        return Objects.equals(left, right);
    }
}
