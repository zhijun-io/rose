package io.zhijun.dev.services.redis;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RoseRedisContainer}.
 */
class RoseRedisContainerIT {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void exposesConnectionDetailsWhenStarted() {
        RoseRedisContainer container = new RoseRedisContainer(new RedisDevServicesProperties());
        container.start();
        try {
            assertThat(container.getRedisHost()).isNotBlank();
            assertThat(container.getRedisPort()).isPositive();
        }
        finally {
            container.stop();
        }
    }

}
