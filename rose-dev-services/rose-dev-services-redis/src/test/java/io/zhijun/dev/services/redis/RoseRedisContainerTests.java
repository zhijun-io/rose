package io.zhijun.dev.services.redis;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseRedisContainer}.
 */
class RoseRedisContainerTests extends BaseDevServicesContainerTests<RoseRedisContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRedisContainer container = new RoseRedisContainer(new RedisDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RedisDevServicesProperties properties = new RedisDevServicesProperties();
        properties.setPort(1234);

        RoseRedisContainer container = new RoseRedisContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseRedisContainer.REDIS_PORT)));
    }

}
