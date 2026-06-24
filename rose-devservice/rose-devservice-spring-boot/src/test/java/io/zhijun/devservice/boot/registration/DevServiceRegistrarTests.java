package io.zhijun.devservice.boot.registration;

import java.util.Map;
import java.util.function.Consumer;

import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistrationFactoryBean;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Unit test for {@link DevServiceRegistrar}.
 */
class DevServiceRegistrarTests {

    private final DefaultListableBeanFactory beanDefinitionRegistry = new DefaultListableBeanFactory();

    private final StandardEnvironment environment = new StandardEnvironment();

    @Test
    void basicRegistration() {
        doRegister(registry -> registry.registerDevService(service ->
                service.name("docling")
                        .description("Docling")
                        .container(container -> container
                                .type(TestDoclingContainer.class)
                                .supplier(new java.util.function.Supplier<GenericContainer<?>>() {
                                    @Override
                                    public GenericContainer<?> get() {
                                        return new TestDoclingContainer();
                                    }
                                }))));

        assertRegistryExists();
        assertContainerBeanDefinition("docling", TestDoclingContainer.class);
        assertDescriptionBeanDefinition("docling");
        assertBeanDefinitionCount(2);
    }

    @Test
    void multipleRegistrationsFromSingleRegistryInvocation() {
        doRegister(registry -> {
            registry.registerDevService(service ->
                    service.name("docling")
                            .description("Docling")
                            .container(container -> container
                                    .type(TestDoclingContainer.class)
                                    .supplier(new java.util.function.Supplier<GenericContainer<?>>() {
                                        @Override
                                        public GenericContainer<?> get() {
                                            return new TestDoclingContainer();
                                        }
                                    })));

            registry.registerDevService(service ->
                    service.name("postgres")
                            .description("PostgreSQL database")
                            .container(container -> container
                                    .type(TestPostgresContainer.class)
                                    .supplier(new java.util.function.Supplier<GenericContainer<?>>() {
                                        @Override
                                        public GenericContainer<?> get() {
                                            return new TestPostgresContainer();
                                        }
                                    })));
        });

        assertRegistryExists();
        assertContainerBeanDefinition("docling", TestDoclingContainer.class);
        assertDescriptionBeanDefinition("docling");
        assertContainerBeanDefinition("postgres", TestPostgresContainer.class);
        assertDescriptionBeanDefinition("postgres");
        assertBeanDefinitionCount(4);
    }

    @Test
    void whenDuplicateRegistrationThenThrow() {
        doRegister(
                registry -> registry.registerDevService(service ->
                        service.name("docling")
                                .container(container -> container
                                        .type(TestDoclingContainer.class)
                                        .supplier(new java.util.function.Supplier<GenericContainer<?>>() {
                                            @Override
                                            public GenericContainer<?> get() {
                                                return new TestDoclingContainer();
                                            }
                                        }))));

        assertThatIllegalStateException()
                .isThrownBy(() -> doRegister(
                        registry -> registry.registerDevService(service ->
                                service.name("docling")
                                        .container(container -> container
                                                .type(TestDoclingContainer.class)
                                                .supplier(new java.util.function.Supplier<GenericContainer<?>>() {
                                                    @Override
                                                    public GenericContainer<?> get() {
                                                        return new TestDoclingContainer();
                                                    }
                                                })))))
                .withMessageContaining("Dev service already registered: docling");
    }

    @Test
    void noRegistrations() {
        doRegister(new Consumer<DevServiceRegistry>() {
            @Override
            public void accept(DevServiceRegistry registry) {
            }
        });

        assertRegistryExists();
        assertBeanDefinitionCount(0);
    }

    @Test
    void setDefaultPropertyAddsToDefaultPropertiesSource() {
        TestRegistrar registrar = new TestRegistrar(new Consumer<DevServiceRegistry>() {
            @Override
            public void accept(DevServiceRegistry registry) {
            }
        }, environment, beanDefinitionRegistry);
        registrar.registerBeanDefinitions(AnnotationMetadata.introspect(this.getClass()), beanDefinitionRegistry);

        registrar.setDefaultProperty("spring.datasource.url", "jdbc:h2:mem:test");

        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:h2:mem:test");
    }

    @Test
    void setDefaultPropertyIsOverriddenByHigherPrioritySource() {
        MutablePropertySources sources = environment.getPropertySources();
        Map<String, Object> userConfig = new java.util.HashMap<String, Object>();
        userConfig.put("spring.datasource.url", "jdbc:postgresql://user-host/db");
        sources.addFirst(new MapPropertySource("userConfig", userConfig));

        TestRegistrar registrar = new TestRegistrar(new Consumer<DevServiceRegistry>() {
            @Override
            public void accept(DevServiceRegistry registry) {
            }
        }, environment, beanDefinitionRegistry);
        registrar.registerBeanDefinitions(AnnotationMetadata.introspect(this.getClass()), beanDefinitionRegistry);

        registrar.setDefaultProperty("spring.datasource.url", "jdbc:h2:mem:default");

        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://user-host/db");
    }

    private void doRegister(Consumer<DevServiceRegistry>... registrars) {
        for (Consumer<DevServiceRegistry> consumer : registrars) {
            TestRegistrar registrar = new TestRegistrar(consumer, environment, beanDefinitionRegistry);
            registrar.registerBeanDefinitions(AnnotationMetadata.introspect(this.getClass()), beanDefinitionRegistry);
        }
    }

    private void assertContainerBeanDefinition(String serviceName, Class<?> containerType) {
        String beanName = "devService.container." + serviceName;
        assertThat(beanDefinitionRegistry.containsBeanDefinition(beanName)).isTrue();

        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        assertThat(beanDefinition.getBeanClassName()).isEqualTo(containerType.getName());
        assertThat(beanDefinition.getRole()).isEqualTo(BeanDefinition.ROLE_INFRASTRUCTURE);
    }

    private void assertDescriptionBeanDefinition(String serviceName) {
        String beanName = "devServiceRegistration." + serviceName;
        assertThat(beanDefinitionRegistry.containsBeanDefinition(beanName)).isTrue();

        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        assertThat(beanDefinition.getBeanClassName()).isEqualTo(DevServiceRegistrationFactoryBean.class.getName());
        assertThat(beanDefinition.getRole()).isEqualTo(BeanDefinition.ROLE_SUPPORT);

        String[] dependsOn = beanDefinition.getDependsOn();
        assertThat(dependsOn).isNotNull();
        assertThat(dependsOn).contains("devService.container." + serviceName);
    }

    private void assertBeanDefinitionCount(int count) {
        assertThat(beanDefinitionRegistry.getBeanDefinitionCount()).isEqualTo(count);
    }

    private void assertRegistryExists() {
        assertThat(beanDefinitionRegistry.containsSingleton(DevServiceRegistrar.DEV_SERVICES_REGISTRY_BEAN_NAME)).isTrue();
    }

    private static class TestRegistrar extends DevServiceRegistrar {

        private final Consumer<DevServiceRegistry> registrar;

        TestRegistrar(Consumer<DevServiceRegistry> registrar, Environment environment,
                      DefaultListableBeanFactory beanDefinitionRegistry) {
            this.registrar = registrar;
            setEnvironment(environment);
            setBeanFactory(beanDefinitionRegistry);
        }

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            registrar.accept(registry);
        }
    }

    private static class TestDoclingContainer extends GenericContainer<TestDoclingContainer> {
        TestDoclingContainer() {
            super("docling");
        }
    }

    private static class TestPostgresContainer extends GenericContainer<TestPostgresContainer> {
        TestPostgresContainer() {
            super("postgres");
        }
    }
}
