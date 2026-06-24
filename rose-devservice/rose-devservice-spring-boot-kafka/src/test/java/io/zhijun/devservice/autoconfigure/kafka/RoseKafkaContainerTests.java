package io.zhijun.devservice.autoconfigure.kafka;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseKafkaContainer}.
 */
class RoseKafkaContainerTests extends BaseDevServicesContainerTests<RoseKafkaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseKafkaContainer container = new RoseKafkaContainer(new KafkaDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        KafkaDevServiceProperties properties = new KafkaDevServiceProperties();
        properties.setPort(1234);

        RoseKafkaContainer container = new RoseKafkaContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseKafkaContainer.KAFKA_PORT)));
    }

}
