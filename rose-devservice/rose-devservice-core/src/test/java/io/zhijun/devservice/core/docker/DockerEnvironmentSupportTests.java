package io.zhijun.devservice.core.docker;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DockerEnvironmentSupport}.
 */
class DockerEnvironmentSupportTests {

    private String originalChecksDisableEnv;
    private String originalDockerApiVersionEnv;
    private String originalDockerHostEnv;
    private String originalChecksDisableProperty;
    private String originalDockerApiVersionProperty;
    private String originalApiVersionProperty;

    @BeforeEach
    void captureEnvironment() {
        originalChecksDisableEnv = System.getenv("TESTCONTAINERS_CHECKS_DISABLE");
        originalDockerApiVersionEnv = System.getenv("DOCKER_API_VERSION");
        originalDockerHostEnv = System.getenv("DOCKER_HOST");
        originalChecksDisableProperty = System.getProperty("TESTCONTAINERS_CHECKS_DISABLE");
        originalDockerApiVersionProperty = System.getProperty("DOCKER_API_VERSION");
        originalApiVersionProperty = System.getProperty("api.version");
    }

    @AfterEach
    void restoreEnvironment() {
        restoreProperty("TESTCONTAINERS_CHECKS_DISABLE", originalChecksDisableProperty);
        restoreProperty("DOCKER_API_VERSION", originalDockerApiVersionProperty);
        restoreProperty("api.version", originalApiVersionProperty);
    }

    @Test
    void configureIfNeededSetsDefaultsWhenUnset() {
        System.clearProperty("TESTCONTAINERS_CHECKS_DISABLE");
        System.clearProperty("DOCKER_API_VERSION");
        System.clearProperty("api.version");

        DockerEnvironmentSupport.configureIfNeeded();

        assertThat(System.getProperty("TESTCONTAINERS_CHECKS_DISABLE")).isEqualTo("true");
        assertThat(System.getProperty("DOCKER_API_VERSION")).isEqualTo("1.44");
        assertThat(System.getProperty("api.version")).isEqualTo("1.44");
    }

    @Test
    void configureIfNeededUsesLocalDockerSocketWhenAvailable() {
        Assumptions.assumeTrue(
                new File("/var/run/docker.sock").exists()
                        || new File(System.getProperty("user.home") + "/.orbstack/run/docker.sock").exists(),
                "Docker socket not available");

        DockerEnvironmentSupport.configureIfNeeded();
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
