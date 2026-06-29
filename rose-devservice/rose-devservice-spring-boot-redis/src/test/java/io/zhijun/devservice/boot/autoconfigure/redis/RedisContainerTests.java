package io.zhijun.devservice.boot.autoconfigure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

/**
 * Unit test for {@link RedisContainer}.
 */
class RedisContainerTests extends BaseDevServicesContainerTests<RedisContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RedisContainer container = new RedisContainer(new RedisDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RedisDevServiceProperties properties = new RedisDevServiceProperties();
        properties.setPort(1234);

        RedisContainer container = new RedisContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(
                                binding -> binding.startsWith(properties.getPort() + ":" + RedisContainer.REDIS_PORT)));
    }
}
