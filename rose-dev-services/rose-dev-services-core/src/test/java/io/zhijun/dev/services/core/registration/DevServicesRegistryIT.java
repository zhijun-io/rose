package io.zhijun.dev.services.core.registration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import io.zhijun.dev.services.api.registration.ContainerInfo;
import io.zhijun.dev.services.core.docker.DockerEnvironmentSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DevServicesRegistry}.
 */
class DevServicesRegistryIT {

    static {
        DockerEnvironmentSupport.configureIfNeeded();
    }

    @Test
    void extractContainerInfoByIdReturnsMetadataForRunningContainer() {
        TestPostgresContainer container = new TestPostgresContainer();
        container.start();
        try {
            ContainerInfo info = DevServicesRegistry.extractContainerInfoById(container.getContainerId());

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
