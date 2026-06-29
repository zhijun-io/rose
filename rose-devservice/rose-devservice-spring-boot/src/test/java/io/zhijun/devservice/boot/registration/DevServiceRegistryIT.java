package io.zhijun.devservice.boot.registration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import io.zhijun.devservice.core.api.registration.ContainerInfo;
import io.zhijun.devservice.core.docker.DockerEnvironmentSupport;

/**
 * Integration test for {@link DevServiceRegistry}.
 */
class DevServiceRegistryIT {

    static {
        DockerEnvironmentSupport.configureIfNeeded();
    }

    @Test
    void extractContainerInfoByIdReturnsMetadataForRunningContainer() {
        TestPostgresContainer container = new TestPostgresContainer();
        container.start();
        try {
            ContainerInfo info = DevServiceRegistry.extractContainerInfoById(container.getContainerId());

            assertThat(info.getId()).isEqualTo(container.getContainerId());
            assertThat(info.getImageName()).isNotBlank();
            assertThat(info.getStatus()).isNotBlank();
        } finally {
            container.stop();
        }
    }

    private static class TestPostgresContainer extends GenericContainer<TestPostgresContainer> {
        TestPostgresContainer() {
            super("postgres:latest");
        }
    }
}
