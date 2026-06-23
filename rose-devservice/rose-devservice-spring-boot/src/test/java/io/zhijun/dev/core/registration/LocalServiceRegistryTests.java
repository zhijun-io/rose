package io.zhijun.dev.core.registration;

import io.zhijun.dev.core.registration.LocalServiceRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit test for {@link LocalServiceRegistry}.
 */
class LocalServiceRegistryTests {

    private final LocalServiceRegistry registry = new LocalServiceRegistry(new DefaultListableBeanFactory());

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
        LocalServiceRegistry validRegistry = new LocalServiceRegistry(factory);

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
        LocalServiceRegistry validRegistry = new LocalServiceRegistry(factory);
        Consumer<LocalServiceRegistry.ServiceSpec> registration = service -> service
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
        LocalServiceRegistry registry = new LocalServiceRegistry(factory);

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
