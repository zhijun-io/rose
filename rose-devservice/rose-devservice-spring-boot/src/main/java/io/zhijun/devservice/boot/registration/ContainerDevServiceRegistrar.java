package io.zhijun.devservice.boot.registration;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import io.zhijun.core.annotation.Incubating;

/**
 * Base registrar for non-JDBC dev service connectors backed by a single Testcontainers
 * {@link Container} bean.
 *
 * @param <P> dev service properties type
 * @param <C> Testcontainers container type
 */
@Incubating
public abstract class ContainerDevServiceRegistrar<P, C extends Container<?> & Startable> extends DevServiceRegistrar {

    protected abstract Class<P> getPropertiesType();

    protected abstract String getConfigPrefix();

    protected abstract String getServiceName();

    protected abstract String getDisplayName();

    protected abstract Class<C> getContainerClass();

    protected abstract C createContainer(P properties);

    /**
     * Register dynamic Spring properties resolved against the running container.
     */
    protected abstract void registerDynamicProperties();

    @Override
    protected final void registerDevServices(DevServiceRegistry registry, Environment environment) {
        P properties = bindProperties(getConfigPrefix(), getPropertiesType());

        registry.registerDevService(getServiceName(), getDisplayName(), getContainerClass(),
                () -> createContainer(properties));

        registerDynamicProperties();
    }

    /**
     * Returns the container bean, starting it if necessary. Safe to call from dynamic property suppliers.
     */
    protected final C requireRunningContainer() {
        C container = getBeanFactory().getBean(getContainerClass());
        ensureContainerStarted(container, getServiceName());
        return container;
    }

}
