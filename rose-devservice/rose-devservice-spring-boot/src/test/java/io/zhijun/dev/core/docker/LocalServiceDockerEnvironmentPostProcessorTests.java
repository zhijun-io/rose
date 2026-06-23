package io.zhijun.dev.core.docker;

import io.zhijun.dev.core.docker.LocalServiceDockerEnvironmentPostProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link LocalServiceDockerEnvironmentPostProcessor}.
 */
class LocalServiceDockerEnvironmentPostProcessorTests {

    private final LocalServiceDockerEnvironmentPostProcessor postProcessor =
            new LocalServiceDockerEnvironmentPostProcessor();

    private String originalChecksDisableProperty;
    private String originalDockerApiVersionProperty;
    private String originalApiVersionProperty;

    @BeforeEach
    void captureEnvironment() {
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
    void hasHighestPrecedence() {
        assertThat(postProcessor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void postProcessEnvironmentConfiguresDockerSupport() {
        System.clearProperty("TESTCONTAINERS_CHECKS_DISABLE");
        System.clearProperty("DOCKER_API_VERSION");
        System.clearProperty("api.version");

        postProcessor.postProcessEnvironment(new MockEnvironment(), new SpringApplication());

        assertThat(System.getProperty("TESTCONTAINERS_CHECKS_DISABLE")).isEqualTo("true");
        assertThat(System.getProperty("DOCKER_API_VERSION")).isEqualTo("1.44");
        assertThat(System.getProperty("api.version")).isEqualTo("1.44");
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

}
