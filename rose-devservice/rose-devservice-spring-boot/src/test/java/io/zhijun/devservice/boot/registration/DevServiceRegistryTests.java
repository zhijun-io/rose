package io.zhijun.devservice.boot.registration;

import io.zhijun.devservice.boot.registration.DevServiceRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Unit test for {@link DevServiceRegistry}.
 */
class DevServiceRegistryTests {

    private final DevServiceRegistry registry = new DevServiceRegistry(new DefaultListableBeanFactory());

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
        DevServiceRegistry validRegistry = new DevServiceRegistry(factory);

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
    void whenServiceAlreadyRegisteredThenThrow() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DevServiceRegistry validRegistry = new DevServiceRegistry(factory);
        Consumer<DevServiceRegistry.ServiceSpec> registration = service -> service
                .name("postgres")
                .container(container -> container
                        .type(TestPostgresContainer.class)
                        .supplier(TestPostgresContainer::new));

        validRegistry.registerDevService(registration);

        assertThatIllegalStateException()
                .isThrownBy(() -> validRegistry.registerDevService(registration))
                .withMessageContaining("Dev service already registered: postgres");
    }

    @Test
    void registeredContainerCanBeResolvedFromBeanFactory() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DevServiceRegistry registry = new DevServiceRegistry(factory);

        registry.registerDevService(service -> service
                .name("postgres")
                .container(container -> container
                        .type(TestPostgresContainer.class)
                        .supplier(TestPostgresContainer::new)));

        AbstractBeanDefinition beanDefinition =
                (AbstractBeanDefinition) factory.getBeanDefinition("devService.container.postgres");
        assertThat(beanDefinition.getBeanClassName()).isEqualTo(TestPostgresContainer.class.getName());
        assertThat(beanDefinition.getInstanceSupplier()).isNotNull();
    }

    private static class TestPostgresContainer extends GenericContainer<TestPostgresContainer> {
        TestPostgresContainer() {
            super("postgres:latest");
        }
    }

}
