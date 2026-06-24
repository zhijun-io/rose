package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseRabbitMqContainer}.
 */
class RoseRabbitMqContainerTests extends BaseDevServicesContainerTests<RoseRabbitMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRabbitMqContainer container = new RoseRabbitMqContainer(new RabbitMqDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        RabbitMqDevServiceProperties properties = new RabbitMqDevServiceProperties();
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
