package io.zhijun.spring.env;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

/**
 * Utilities for working with {@link PropertySources}.
 */
public abstract class PropertySourcesUtils {

    private PropertySourcesUtils() {}

    public static Map<String, Object> getSubProperties(ConfigurableEnvironment environment, String prefix) {
        return getSubProperties(environment.getPropertySources(), environment, prefix);
    }

    public static Map<String, Object> getSubProperties(
            PropertySources propertySources, PropertyResolver propertyResolver, String prefix) {
        Map<String, Object> subProperties = new LinkedHashMap<String, Object>();
        String normalizedPrefix = normalizePrefix(prefix);
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        while (iterator.hasNext()) {
            PropertySource<?> source = iterator.next();
            for (String name : getPropertyNames(source)) {
                if (!subProperties.containsKey(name) && name.startsWith(normalizedPrefix)) {
                    String subName = name.substring(normalizedPrefix.length());
                    if (!subProperties.containsKey(subName)) {
                        Object value = source.getProperty(name);
                        if (value instanceof String) {
                            value = propertyResolver.resolvePlaceholders((String) value);
                        }
                        subProperties.put(subName, value);
                    }
                }
            }
        }
        return subProperties;
    }

    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }

    private static String[] getPropertyNames(PropertySource<?> propertySource) {
        if (propertySource instanceof EnumerablePropertySource) {
            return ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
        }
        return new String[0];
    }
}
