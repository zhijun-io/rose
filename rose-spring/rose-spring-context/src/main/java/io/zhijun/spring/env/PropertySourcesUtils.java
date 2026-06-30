package io.zhijun.spring.env;

import io.zhijun.core.annotation.Nullable;
import org.springframework.core.env.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class PropertySourcesUtils {

    public static final String DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME = "defaultProperties";

    @Nullable
    public static Map<String, Object> getDefaultProperties(ConfigurableEnvironment environment) {
        PropertySource<?> propertySource = environment.getPropertySources().get(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME);
        if (propertySource instanceof MapPropertySource) {
            return ((MapPropertySource) propertySource).getSource();
        }
        return null;
    }

    public static Set<String> findPropertyNames(ConfigurableEnvironment environment, Predicate<String> filter) {
        Set<String> propertyNames = new java.util.LinkedHashSet<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                for (String propertyName : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                    if (filter.test(propertyName)) {
                        propertyNames.add(propertyName);
                    }
                }
            }
        }
        return propertyNames;
    }

    public static Map<String, String> getSubProperties(PropertySources propertySources, String prefix) {
        Map<String, String> subProperties = new LinkedHashMap<>();
        String normalizedPrefix = normalizePrefix(prefix);
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource) propertySource).getPropertyNames();
                for (String propertyName : propertyNames) {
                    if (propertyName.startsWith(normalizedPrefix)) {
                        String subKey = propertyName.substring(normalizedPrefix.length());
                        Object value = propertySource.getProperty(propertyName);
                        if (value != null) {
                            subProperties.put(subKey, value.toString());
                        }
                    }
                }
            }
        }
        return subProperties;
    }

    public static Map<String, Object> getSubProperties(PropertySources propertySources,
                                                        ConfigurableEnvironment environment,
                                                        String prefix) {
        Map<String, Object> subProperties = new LinkedHashMap<>();
        String normalizedPrefix = normalizePrefix(prefix);
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource) propertySource).getPropertyNames();
                for (String propertyName : propertyNames) {
                    if (propertyName.startsWith(normalizedPrefix)) {
                        String subKey = propertyName.substring(normalizedPrefix.length());
                        Object value = propertySource.getProperty(propertyName);
                        if (value != null) {
                            subProperties.put(subKey, value);
                        }
                    }
                }
            }
        }
        return subProperties;
    }

    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }
}
