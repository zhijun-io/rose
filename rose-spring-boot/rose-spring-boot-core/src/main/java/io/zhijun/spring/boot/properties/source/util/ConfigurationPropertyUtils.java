package io.zhijun.spring.boot.properties.source.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.DataObjectPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * {@link ConfigurationProperty} 工具类
 */
public abstract class ConfigurationPropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPropertyUtils.class);

    public static String getPrefix(ConfigurationPropertyName name, BindContext context) {
        int depth = context.getDepth();
        if (name.isLastElementIndexed()) {
            name = getParent(name);
            depth--;
        }
        String propertyName = name.toString();
        if (depth == 0) {
            return propertyName;
        }
        String prefix = propertyName;
        for (int i = 0; i < depth; i++) {
            int lastDot = prefix.lastIndexOf('.');
            if (lastDot > 0) {
                prefix = prefix.substring(0, lastDot);
            }
        }
        if (propertyName.equalsIgnoreCase(prefix)) {
            prefix = getSource(name);
        }
        return prefix;
    }

    public static String getSource(ConfigurationPropertyName name) {
        return name.toString();
    }

    public static ConfigurationPropertyName getParent(ConfigurationPropertyName name) {
        return name.getParent();
    }

    public static String toDashedForm(String name) {
        return DataObjectPropertyName.toDashedForm(name);
    }

    private ConfigurationPropertyUtils() {
    }
}
