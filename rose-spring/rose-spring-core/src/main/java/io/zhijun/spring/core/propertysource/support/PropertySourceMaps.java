package io.zhijun.spring.core.propertysource.support;

import java.lang.reflect.Array;
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

    /**
     * Normalizes a leaf property value to {@link String}, matching {@link java.util.Properties} semantics.
     */
    public static String normalizePropertyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Boolean || value instanceof Number || value instanceof Character) {
            return String.valueOf(value);
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private static void flattenInto(Map<String, Object> target, String prefix, Map<?, ?> source) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            putFlattenedValue(target, path, entry.getValue());
        }
    }

    private static void putFlattenedValue(Map<String, Object> target, String path, Object value) {
        if (value instanceof Map) {
            flattenInto(target, path, (Map<?, ?>) value);
            return;
        }
        if (value instanceof Iterable) {
            flattenIterable(target, path, (Iterable<?>) value);
            return;
        }
        if (value != null && value.getClass().isArray()) {
            flattenArray(target, path, value);
            return;
        }
        if (value != null) {
            target.put(path, value);
        }
    }

    private static void flattenIterable(Map<String, Object> target, String prefix, Iterable<?> source) {
        int index = 0;
        for (Object element : source) {
            putFlattenedValue(target, prefix + "[" + index + "]", element);
            index++;
        }
    }

    private static void flattenArray(Map<String, Object> target, String prefix, Object source) {
        int length = Array.getLength(source);
        for (int index = 0; index < length; index++) {
            putFlattenedValue(target, prefix + "[" + index + "]", Array.get(source, index));
        }
    }
}
