package io.zhijun.dev.core.registration;

import io.zhijun.dev.core.registration.LocalServiceRegistry;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import io.zhijun.dev.api.registration.ContainerInfo;
import io.zhijun.dev.core.docker.DockerEnvironmentSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LocalServiceRegistry}.
 */
class LocalServiceRegistryIT {

    static {
        DockerEnvironmentSupport.configureIfNeeded();
    }

    @Test
    void extractContainerInfoByIdReturnsMetadataForRunningContainer() {
        TestPostgresContainer container = new TestPostgresContainer();
        container.start();
        try {
            ContainerInfo info = LocalServiceRegistry.extractContainerInfoById(container.getContainerId());

            assertThat(info.getId()).isEqualTo(container.getContainerId());
            assertThat(info.getImageName()).isNotBlank();
            assertThat(info.getStatus()).isNotBlank();
        }
        finally {
            container.stop();
        }
    }

    private static class TestPostgresContainer extends GenericContainer<TestPostgresContainer> {
        TestPostgresContainer() {
            super("postgres:latest");
        }
    }

}
