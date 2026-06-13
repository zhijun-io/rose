package io.zhijun.dev.services.core.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.testcontainers.containers.GenericContainer;

import io.zhijun.dev.services.api.registration.ContainerInfo;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for {@link DevServicesRegistry}.
 */
class DevServicesRegistryTests {

    private final DevServicesRegistry registry = new DevServicesRegistry(new DefaultListableBeanFactory());

    @Test
    void whenServiceNameIsNullThenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.registerDevService(service -> service
                        .name(null)
                        .container(container -> container
                                .type(TestPostgresContainer.class)
                                .supplier(TestPostgresContainer::new))))
                .withMessageContaining("service name cannot be null or empty");
    }

    @Test
    void whenServiceNameIsEmptyThenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.registerDevService(service -> service
                        .name("")
                        .container(container -> container
                                .type(TestPostgresContainer.class)
                                .supplier(TestPostgresContainer::new))))
                .withMessageContaining("service name cannot be null or empty");
    }

    @Test
    void whenContainerSpecIsNullThenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.registerDevService(service -> service
                        .name("postgres")))
                .withMessageContaining("service container cannot be null");
    }

    @Test
    void whenContainerTypeIsNullThenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.registerDevService(service -> service
                        .name("postgres")
                        .container(container -> container
                                .type(null)
                                .supplier(TestPostgresContainer::new))))
                .withMessageContaining("container type cannot be null");
    }

    @Test
    void whenContainerSupplierIsNullThenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.registerDevService(service -> service
                        .name("postgres")
                        .container(container -> container
                                .type(TestPostgresContainer.class)
                                .supplier(null))))
                .withMessageContaining("container supplier cannot be null");
    }

    @Test
    void whenValidServiceThenRegisterBeanDefinitions() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DevServicesRegistry validRegistry = new DevServicesRegistry(factory);

        validRegistry.registerDevService(service -> service
                .name("postgres")
                .description("PostgreSQL")
                .container(container -> container
                        .type(TestPostgresContainer.class)
                        .supplier(TestPostgresContainer::new)));

        assertThat(factory.containsBeanDefinition("devService.container.postgres")).isTrue();
        assertThat(factory.containsBeanDefinition("devServiceRegistration.postgres")).isTrue();
    }

    @Test
    void whenServiceAlreadyRegisteredThenSkipDuplicateDefinitions() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DevServicesRegistry validRegistry = new DevServicesRegistry(factory);
        Consumer<DevServicesRegistry.ServiceSpec> registration = service -> service
                .name("postgres")
                .container(container -> container
                        .type(TestPostgresContainer.class)
                        .supplier(TestPostgresContainer::new));

        validRegistry.registerDevService(registration);
        validRegistry.registerDevService(registration);

        assertThat(factory.getBeanDefinitionNames()).hasSize(2);
    }

    @Test
    void registeredContainerCanBeResolvedFromBeanFactory() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DevServicesRegistry registry = new DevServicesRegistry(factory);

        registry.registerDevService(service -> service
                .name("postgres")
                .container(container -> container
                        .type(TestPostgresContainer.class)
                        .supplier(TestPostgresContainer::new)));

        TestPostgresContainer container = factory.getBean("devService.container.postgres", TestPostgresContainer.class);

        assertThat(container).isNotNull();
        assertThat(container.getDockerImageName()).contains("postgres");
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
