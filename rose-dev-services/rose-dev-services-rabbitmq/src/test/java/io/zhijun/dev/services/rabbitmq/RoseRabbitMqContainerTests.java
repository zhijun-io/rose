package io.zhijun.dev.services.rabbitmq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseRabbitMqContainer}.
 */
class RoseRabbitMqContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseRabbitMqContainer container = new RoseRabbitMqContainer(new RabbitMqDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        RabbitMqDevServicesProperties properties = new RabbitMqDevServicesProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseRabbitMqContainer container = new RoseRabbitMqContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseRabbitMqContainer.AMQP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseRabbitMqContainer.HTTP_PORT));
    }

}
