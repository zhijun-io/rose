package io.zhijun.dev.services.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseKafkaContainer}.
 */
class RoseKafkaContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseKafkaContainer container = new RoseKafkaContainer(new KafkaDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        KafkaDevServicesProperties properties = new KafkaDevServicesProperties();
        properties.setPort(1234);

        RoseKafkaContainer container = new RoseKafkaContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseKafkaContainer.KAFKA_PORT));
    }

}
