package io.zhijun.devservice.core.util;

/**
 * Minimal assertions for devservice-core (no Spring dependency).
 */
public final class DevServiceAssert {

    private DevServiceAssert() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, String message) {
        if (!DevServiceStrings.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

}
