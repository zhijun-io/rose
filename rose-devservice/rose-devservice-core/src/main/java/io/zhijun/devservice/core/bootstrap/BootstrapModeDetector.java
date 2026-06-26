package io.zhijun.devservice.core.bootstrap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

/**
 * Detects bootstrap mode from environment and stack trace heuristics.
 */
final class BootstrapModeDetector {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapModeDetector.class);

    private static final Set<String> TEST_CLASS_PREFIXES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "org.junit.runners.",
            "org.junit.jupiter.",
            "org.junit.platform.",
            "org.springframework.boot.test.",
            "cucumber.runtime."
    )));

    private static volatile BootstrapMode cachedMode;
    private static final Object LOCK = new Object();

    static BootstrapMode detect() {
        return detect(null);
    }

    static BootstrapMode detect(StackTraceElement[] stackTraceElements) {
        if (cachedMode == null) {
            synchronized (LOCK) {
                if (cachedMode == null) {
                    cachedMode = doDetect(stackTraceElements);
                }
            }
        }
        return cachedMode;
    }

    static void clearCache() {
        cachedMode = null;
    }

    private static BootstrapMode doDetect(StackTraceElement[] stackTraceElements) {
        String modeProperty = System.getenv(BootstrapMode.PROPERTY_KEY.toUpperCase().replace(".", "_"));
        if (!StringUtils.isNotBlank(modeProperty)) {
            modeProperty = System.getProperty(BootstrapMode.PROPERTY_KEY);
        }
        if (StringUtils.isNotBlank(modeProperty)) {
            String normalized = modeProperty.trim().toUpperCase();
            if (BootstrapMode.isValid(normalized)) {
                return BootstrapMode.valueOf(normalized);
            }
            logger.warn("Invalid {} property value: '{}'. Defaulting to PROD mode.",
                    BootstrapMode.PROPERTY_KEY, modeProperty);
            return BootstrapMode.PROD;
        }

        long startTime = System.nanoTime();
        StackTraceElement[] stackTrace = (stackTraceElements == null || stackTraceElements.length == 0)
                ? Thread.currentThread().getStackTrace()
                : stackTraceElements;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("SpringApplicationAotProcessor")) {
                logger.debug("Prod bootstrap mode detection from stack trace took {} ns",
                        System.nanoTime() - startTime);
                return BootstrapMode.PROD;
            }
            for (String prefix : TEST_CLASS_PREFIXES) {
                if (className.startsWith(prefix)) {
                    logger.debug("Test bootstrap mode detection from stack trace took {} ns",
                            System.nanoTime() - startTime);
                    return BootstrapMode.TEST;
                }
            }
        }
        logger.debug("Bootstrap mode detection from stack trace took {} ns", System.nanoTime() - startTime);

        if (isNativeContext()) {
            return BootstrapMode.PROD;
        }

        if (isDevelopmentContext()) {
            return BootstrapMode.DEV;
        }

        return BootstrapMode.PROD;
    }

    static boolean isNativeContext() {
        return ClassUtils.isPresent("org.graalvm.nativeimage.ImageInfo", null);
    }

    static boolean isDevelopmentContext() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (ClassUtils.isPresent("org.springframework.boot.devtools.RemoteSpringApplication", classLoader)) {
            return true;
        }
        if (classLoader != null && classLoader.getClass().getName().contains("AppClassLoader")) {
            return true;
        }
        return false;
    }
}
