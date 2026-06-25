package io.zhijun.devservice.core.util;

/**
 * String helpers for devservice-core (no Spring dependency).
 */
public final class DevServiceStrings {

    private DevServiceStrings() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
