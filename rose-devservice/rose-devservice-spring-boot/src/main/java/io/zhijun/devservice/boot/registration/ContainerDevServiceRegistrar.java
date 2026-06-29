package io.zhijun.devservice.boot.registration;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import io.zhijun.annotation.Incubating;

/**
 * Registrar for non-JDBC dev service connectors backed by a single Testcontainers container.
 */
@Incubating
public class ContainerDevServiceRegistrar<P, C extends Container<?> & Startable> extends DevServiceRegistrar {

    private final DevServiceConnectorDescriptor<P, C> descriptor;

    protected ContainerDevServiceRegistrar(DevServiceConnectorDescriptor<P, C> descriptor) {
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

        if (descriptor.dynamicProperties() != null) {
            descriptor.dynamicProperties().register(this);
        }
    }

    /**
     * Returns the container bean, starting it if necessary.
     */
    public final C requireRunningContainer() {
        C container = getBeanFactory().getBean(descriptor.containerClass());
        ensureContainerStarted(container, descriptor.serviceName());
        return container;
    }
}
