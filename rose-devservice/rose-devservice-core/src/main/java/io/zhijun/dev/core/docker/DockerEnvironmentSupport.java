package io.zhijun.dev.core.docker;

import java.io.File;

import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Applies Testcontainers / Docker defaults when the environment is not already configured.
 * Used at application startup and in integration tests.
 */
public final class DockerEnvironmentSupport {

    private static final String DOCKER_API_VERSION = "1.44";

    private static final String ORBSTACK_SOCKET = System.getProperty("user.home")
            + "/.orbstack/run/docker.sock";

    private DockerEnvironmentSupport() {
    }

    /**
     * Applies defaults when Docker env vars are not already set (e.g. local dev, Surefire, CI).
     */
    public static void configureIfNeeded() {
        configureChecksDisable();
        configureDockerApiVersion();
        configureDockerHost();
    }

    private static void configureChecksDisable() {
        if (System.getenv("TESTCONTAINERS_CHECKS_DISABLE") == null) {
            System.setProperty("TESTCONTAINERS_CHECKS_DISABLE", "true");
        }
    }

    private static void configureDockerApiVersion() {
        if (System.getenv("DOCKER_API_VERSION") == null) {
            System.setProperty("DOCKER_API_VERSION", DOCKER_API_VERSION);
        }
        if (System.getProperty("api.version") == null) {
            System.setProperty("api.version", DOCKER_API_VERSION);
        }
    }

    private static void configureDockerHost() {
        if (System.getenv("DOCKER_HOST") != null) {
            return;
        }
        String socket = resolveDockerSocket();
        if (socket != null) {
            TestcontainersConfiguration.getInstance()
                    .updateUserConfig("docker.host", "unix://" + socket);
        }
    }

    private static String resolveDockerSocket() {
        File orbstack = new File(ORBSTACK_SOCKET);
        if (orbstack.exists()) {
            return orbstack.getAbsolutePath();
        }
        File defaultSocket = new File("/var/run/docker.sock");
        if (defaultSocket.exists()) {
            return defaultSocket.getAbsolutePath();
        }
        return null;
    }
}
