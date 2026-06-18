package io.zhijun.dev.services.bootstrap;

import org.springframework.util.StringUtils;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.core.annotation.Internal;

/**
 * Application bootstrap mode.
 */
@Incubating
public enum BootstrapMode {

    DEV,
    TEST,
    PROD;

    public static final String PROPERTY_KEY = "rose.bootstrap.mode";

    public static BootstrapMode detect() {
        return BootstrapModeDetector.detect();
    }

    public static boolean isDev() {
        return detect() == DEV;
    }

    public static boolean isTest() {
        return detect() == TEST;
    }

    @Internal
    public static void clear() {
        BootstrapModeDetector.clearCache();
    }

    static boolean isValid(String modeProperty) {
        if (!StringUtils.hasText(modeProperty)) {
            return false;
        }

        try {
            BootstrapMode.valueOf(modeProperty.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
