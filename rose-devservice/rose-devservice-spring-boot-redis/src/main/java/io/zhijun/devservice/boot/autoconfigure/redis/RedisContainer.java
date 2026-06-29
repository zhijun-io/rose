package io.zhijun.devservice.boot.autoconfigure.redis;

import org.testcontainers.containers.wait.strategy.Wait;

import io.zhijun.devservice.core.container.AbstractDevServiceContainer;

/**
 * Redis container configured for Rose DevService.
 */
final class RedisContainer extends AbstractDevServiceContainer<RedisContainer, RedisDevServiceProperties> {

    static final String COMPATIBLE_IMAGE_NAME =
            org.testcontainers.utility.DockerImageName.parse(RedisDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int REDIS_PORT = 6379;

    RedisContainer(RedisDevServiceProperties properties) {
        super(properties, RedisDevServiceProperties.DEFAULT_IMAGE_NAME, REDIS_PORT);
        waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));
    }

    String getRedisHost() {
        return getHost();
    }

    Integer getRedisPort() {
        return getMappedDefaultPort();
    }

    @Override
    protected void configure() {
        super.configure();
    }
}
