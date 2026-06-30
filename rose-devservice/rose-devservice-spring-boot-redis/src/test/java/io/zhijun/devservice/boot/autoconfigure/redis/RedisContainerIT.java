package io.zhijun.devservice.boot.autoconfigure.redis;

import io.zhijun.devservice.core.docker.DockerEnvironmentSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RedisContainer}.
 */
class RedisContainerIT {

    static {
        DockerEnvironmentSupport.configureIfNeeded();
    }

    @Test
    void exposesConnectionDetailsWhenStarted() {
        RedisContainer container = new RedisContainer(new RedisDevServiceProperties());
        container.start();
        try {
            assertThat(container.getRedisHost()).isNotBlank();
            assertThat(container.getRedisPort()).isPositive();
        } finally {
            container.stop();
        }
    }
}
