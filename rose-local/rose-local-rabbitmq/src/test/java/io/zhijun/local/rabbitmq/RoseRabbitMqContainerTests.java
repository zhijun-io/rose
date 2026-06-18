package io.zhijun.local.rabbitmq;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseRabbitMqContainer}.
 */
class RoseRabbitMqContainerTests extends BaseDevServicesContainerTests<RoseRabbitMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRabbitMqContainer container = new RoseRabbitMqContainer(new RabbitMqLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RabbitMqLocalServiceProperties properties = new RabbitMqLocalServiceProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseRabbitMqContainer container = new RoseRabbitMqContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseRabbitMqContainer.AMQP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseRabbitMqContainer.HTTP_PORT)));
    }

}
