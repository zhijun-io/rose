package io.zhijun.devservice.core.docker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DockerEnvironmentSupportTests {

    private String originalChecksDisable;
    private String originalDockerApiVersion;
    private String originalApiVersionProp;
    private String originalDockerHost;

    @AfterEach
    void tearDown() {
        // Restore original properties/env
        if (originalChecksDisable != null) {
            System.setProperty("TESTCONTAINERS_CHECKS_DISABLE", originalChecksDisable);
        } else {
            System.clearProperty("TESTCONTAINERS_CHECKS_DISABLE");
        }
        if (originalDockerApiVersion != null) {
            System.setProperty("DOCKER_API_VERSION", originalDockerApiVersion);
        } else {
            System.clearProperty("DOCKER_API_VERSION");
        }
        if (originalApiVersionProp != null) {
            System.setProperty("api.version", originalApiVersionProp);
        } else {
            System.clearProperty("api.version");
        }
        if (originalDockerHost != null) {
            System.setProperty("DOCKER_HOST", originalDockerHost);
        } else {
            System.clearProperty("DOCKER_HOST");
        }
    }

    @Test
    void configureIfNeededDoesNotThrow() {
        // Just verify it runs without exceptions on any platform
        assertDoesNotThrow(DockerEnvironmentSupport::configureIfNeeded);
    }

    @Test
    void configureChecksDisableSetsPropertyWhenNotSet() {
        originalChecksDisable = System.getProperty("TESTCONTAINERS_CHECKS_DISABLE");
        System.clearProperty("TESTCONTAINERS_CHECKS_DISABLE");

        DockerEnvironmentSupport.configureIfNeeded();
        assertEquals("true", System.getProperty("TESTCONTAINERS_CHECKS_DISABLE"));
    }

    @Test
    void configureDockerApiVersionSetsWhenNotSet() {
        originalDockerApiVersion = System.getProperty("DOCKER_API_VERSION");
        originalApiVersionProp = System.getProperty("api.version");
        System.clearProperty("DOCKER_API_VERSION");
        System.clearProperty("api.version");

        DockerEnvironmentSupport.configureIfNeeded();
        assertEquals("1.44", System.getProperty("DOCKER_API_VERSION"));
        assertEquals("1.44", System.getProperty("api.version"));
    }
}
