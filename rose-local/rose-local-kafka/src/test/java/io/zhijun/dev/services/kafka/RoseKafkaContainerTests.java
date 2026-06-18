package io.zhijun.dev.services.kafka;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseKafkaContainer}.
 */
class RoseKafkaContainerTests extends BaseDevServicesContainerTests<RoseKafkaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseKafkaContainer container = new RoseKafkaContainer(new KafkaDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        KafkaDevServicesProperties properties = new KafkaDevServicesProperties();
        properties.setPort(1234);

        RoseKafkaContainer container = new RoseKafkaContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseKafkaContainer.KAFKA_PORT)));
    }

}
