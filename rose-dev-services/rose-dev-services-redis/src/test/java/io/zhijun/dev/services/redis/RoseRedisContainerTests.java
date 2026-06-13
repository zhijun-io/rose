package io.zhijun.dev.services.redis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseRedisContainer}.
 */
class RoseRedisContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRedisContainer container = new RoseRedisContainer(new RedisDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        RedisDevServicesProperties properties = new RedisDevServicesProperties();
        properties.setPort(1234);

        RoseRedisContainer container = new RoseRedisContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseRedisContainer.REDIS_PORT));
    }

    @Test
    void exposesConnectionDetailsWhenStarted() {
        RoseRedisContainer container = new RoseRedisContainer(new RedisDevServicesProperties());
        container.start();
        try {
            assertThat(container.getRedisHost()).isNotBlank();
            assertThat(container.getRedisPort()).isPositive();
        } finally {
            container.stop();
        }
    }

}
