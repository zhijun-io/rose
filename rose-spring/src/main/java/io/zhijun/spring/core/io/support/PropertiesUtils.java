package io.zhijun.spring.core.io.support;

import org.jspecify.annotations.Nullable;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Utilities for inline {@link Properties} loading.
 */
public abstract class PropertiesUtils {

    public static Properties loadProperties(String[] values, @Nullable PropertyResolver propertyResolver) throws IOException {
        Properties properties = new Properties();
        String content = StringUtils.arrayToDelimitedString(values, System.lineSeparator());
        String resolved = propertyResolver != null ? propertyResolver.resolvePlaceholders(content) : content;
        properties.load(new StringReader(resolved));
        return properties;
    }

    private PropertiesUtils() {
    }
}
