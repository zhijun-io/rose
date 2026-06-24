package io.zhijun.devservice.boot.registration;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.JdbcDatabaseContainer;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.devservice.core.api.config.AbstractJdbcDevServiceProperties;

/**
 * Base registrar for JDBC dev service connectors that expose {@code spring.datasource.*}.
 *
 * @param <P> dev service properties type
 * @param <C> Testcontainers JDBC container type
 */
@Incubating
public abstract class JdbcDevServiceRegistrar<P extends AbstractJdbcDevServiceProperties,
        C extends JdbcDatabaseContainer<?>> extends DevServiceRegistrar {

    protected abstract Class<P> getPropertiesType();

    protected abstract String getConfigPrefix();

    protected abstract String getServiceName();

    protected abstract String getDisplayName();

    protected abstract Class<C> getContainerClass();

    protected abstract C createContainer(P properties);

    @Override
    protected final void registerDevServices(DevServiceRegistry registry, Environment environment) {
        P properties = bindProperties(getConfigPrefix(), getPropertiesType());

        registry.registerDevService(getServiceName(), getDisplayName(), getContainerClass(),
                () -> createContainer(properties));

        addDynamicProperty("spring.datasource.url", () -> runningContainer().getJdbcUrl());
        addDynamicProperty("spring.datasource.username", () -> runningContainer().getUsername());
        addDynamicProperty("spring.datasource.password", () -> runningContainer().getPassword());
    }

    private C runningContainer() {
        C container = getBeanFactory().getBean(getContainerClass());
        if (!container.isRunning()) {
            container.start();
        }
        return container;
    }

}
