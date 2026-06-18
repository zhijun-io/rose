package io.zhijun.local.tests;

import io.zhijun.local.core.docker.DockerEnvironmentSupport;

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
