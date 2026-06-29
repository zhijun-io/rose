package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

/**
 * Unit test for {@link RabbitMqContainer}.
 */
class RabbitMqContainerTests extends BaseDevServicesContainerTests<RabbitMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RabbitMqContainer container = new RabbitMqContainer(new RabbitMqDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RabbitMqDevServiceProperties properties = new RabbitMqDevServiceProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RabbitMqContainer container = new RabbitMqContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(
                                binding -> binding.startsWith(properties.getPort() + ":" + RabbitMqContainer.AMQP_PORT))
                        .anyMatch(binding -> binding.startsWith(
                                properties.getManagementConsolePort() + ":" + RabbitMqContainer.HTTP_PORT)));
    }
}
