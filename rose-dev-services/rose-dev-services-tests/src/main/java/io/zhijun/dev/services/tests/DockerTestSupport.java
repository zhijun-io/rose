package io.zhijun.dev.services.tests;

import io.zhijun.dev.services.core.docker.DockerEnvironmentSupport;

/**
 * Configures Docker connectivity for Testcontainers before any container starts.
 */
public final class DockerTestSupport {

    private DockerTestSupport() {
    }

    static {
        configureIfNeeded();
    }

    /**
     * Applies defaults when Docker env vars are not already set (e.g. Surefire / CI).
     */
    public static void configureIfNeeded() {
        DockerEnvironmentSupport.configureIfNeeded();
    }
}
