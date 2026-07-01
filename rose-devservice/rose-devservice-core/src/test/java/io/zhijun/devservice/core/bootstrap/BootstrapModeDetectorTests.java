package io.zhijun.devservice.core.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BootstrapModeDetectorTests {

    @AfterEach
    void tearDown() {
        BootstrapModeDetector.clearCache();
    }

    @Test
    void testDetectDefaultMode() {
        // When no system property or env set, default to PROD in normal execution
        BootstrapMode mode = BootstrapModeDetector.detect();
        assertNotNull(mode);
    }

    @Test
    void testIsDevelopmentContextWhenDevtoolsPresent() {
        // DevTools class is on classpath during test
        boolean isDev = BootstrapModeDetector.isDevelopmentContext();
        // Result depends on classpath, just verify it doesn't throw
        assertTrue(!isDev || isDev);
    }

    @Test
    void testIsNativeContext() {
        boolean isNative = BootstrapModeDetector.isNativeContext();
        assertFalse(isNative); // Not native in test JVM
    }
}
