package io.zhijun.devservice.redis;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseRedisContainer}.
 */
class RoseRedisContainerTests extends BaseDevServicesContainerTests<RoseRedisContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRedisContainer container = new RoseRedisContainer(new RedisDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RedisDevServiceProperties properties = new RedisDevServiceProperties();
        properties.setPort(1234);

        RoseRedisContainer container = new RoseRedisContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseRedisContainer.REDIS_PORT)));
    }

}
