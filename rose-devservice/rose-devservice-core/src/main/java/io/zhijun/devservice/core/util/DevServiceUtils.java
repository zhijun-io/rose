package io.zhijun.devservice.core.util;

import io.zhijun.annotation.Incubating;

/**
 * Small helpers for devservice-core (no Spring dependency).
 */
@Incubating
public final class DevServiceUtils {

    private DevServiceUtils() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, String message) {
        if (!hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean isClassPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader != null ? classLoader : DevServiceUtils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
