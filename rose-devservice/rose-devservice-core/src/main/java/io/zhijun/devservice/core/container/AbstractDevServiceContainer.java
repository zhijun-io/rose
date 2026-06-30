package io.zhijun.devservice.core.container;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for all DevService containers, reducing duplicate code across service implementations.
 *
 * @param <C> the container type
 * @param <P> the properties type
 */
public abstract class AbstractDevServiceContainer<C extends AbstractDevServiceContainer<C, P>, P extends BaseDevServiceProperties>
        extends GenericContainer<C> {

    protected final P properties;
    protected final int defaultPort;

    protected AbstractDevServiceContainer(P properties, String defaultImageName, int defaultPort) {
        super(DockerImageName.parse(properties.getImageName())
                .asCompatibleSubstituteFor(DockerImageName.parse(defaultImageName).getUnversionedPart()));
        this.properties = properties;
        this.defaultPort = defaultPort;

        addExposedPorts(defaultPort);
        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), defaultPort);
        }
    }

    /**
     * Returns the mapped port for the default service port.
     */
    public Integer getMappedDefaultPort() {
        return getMappedPort(defaultPort);
    }

    /**
     * Returns the service connection URL in the format "protocol://host:port".
     */
    public String getConnectionUrl(String protocol) {
        return protocol + "://" + getHost() + ":" + getMappedDefaultPort();
    }
}
