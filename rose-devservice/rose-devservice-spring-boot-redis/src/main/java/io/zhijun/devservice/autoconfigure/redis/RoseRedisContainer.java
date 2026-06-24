package io.zhijun.devservice.autoconfigure.redis;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.container.ContainerConfigurer;
import io.zhijun.devservice.util.ContainerUtils;

/**
 * Redis container configured for Rose DevService.
 */
final class RoseRedisContainer extends GenericContainer<RoseRedisContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "redis";

    static final int REDIS_PORT = 6379;

    private final RedisDevServiceProperties properties;

    RoseRedisContainer(RedisDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(REDIS_PORT);
        waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
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
