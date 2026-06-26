package io.zhijun.devservice.boot.autoconfigure.redis;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.container.ContainerConfigurer;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * Redis container configured for Rose DevService.
 */
final class RedisContainer extends GenericContainer<RedisContainer> {

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(RedisDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int REDIS_PORT = 6379;

    private final RedisDevServiceProperties properties;

    RedisContainer(RedisDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(REDIS_PORT);
        waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), REDIS_PORT);
        }
    }

    String getRedisHost() {
        return getHost();
    }

    Integer getRedisPort() {
        return getMappedPort(REDIS_PORT);
    }
}
