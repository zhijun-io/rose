package io.zhijun.spring.core.propertysource.support;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilities for flattening structured configuration values.
 */
public abstract class PropertySourceMaps {

    private PropertySourceMaps() {
    }

    public static Map<String, Object> flatten(Map<?, ?> source) {
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        flattenInto(target, "", source);
        return target;
    }

    @SuppressWarnings("unchecked")
    private static void flattenInto(Map<String, Object> target, String prefix, Map<?, ?> source) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                flattenInto(target, path, (Map<?, ?>) value);
            } else {
                target.put(path, value);
            }
        }
    }
}
