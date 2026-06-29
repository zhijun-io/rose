package io.zhijun.devservice.boot.registration;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;

import io.zhijun.annotation.Incubating;
import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

/**
 * Registrar for JDBC dev service connectors that expose {@code spring.datasource.*}.
 */
@Incubating
public class JdbcDevServiceRegistrar<P extends JdbcDevServiceProperties, C extends JdbcDatabaseContainer<?>>
        extends DevServiceRegistrar {

    private final JdbcDevServiceConnectorDescriptor<P, C> descriptor;

    protected JdbcDevServiceRegistrar(JdbcDevServiceConnectorDescriptor<P, C> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    protected final void registerDevServices(DevServiceRegistry registry, Environment environment) {
        registry.registerDevServiceProvider(descriptor.serviceName(), descriptor.category());

        P properties = bindProperties(descriptor.configPrefix(), descriptor.propertiesType());
        registry.registerDevService(
                descriptor.serviceName(),
                descriptor.displayName(),
                descriptor.containerClass(),
                () -> descriptor.containerFactory().apply(properties));

        addDynamicProperty("spring.datasource.url", () -> runningContainer().getJdbcUrl());
        addDynamicProperty(
                "spring.datasource.username", () -> runningContainer().getUsername());
        addDynamicProperty(
                "spring.datasource.password", () -> runningContainer().getPassword());
    }

    private C runningContainer() {
        C container = getBeanFactory().getBean(descriptor.containerClass());
        ensureContainerStarted(container, descriptor.serviceName());
        return container;
    }
}
