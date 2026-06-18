package io.zhijun.local.kafka;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseKafkaContainer}.
 */
class RoseKafkaContainerTests extends BaseDevServicesContainerTests<RoseKafkaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseKafkaContainer container = new RoseKafkaContainer(new KafkaLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        KafkaLocalServiceProperties properties = new KafkaLocalServiceProperties();
        properties.setPort(1234);

        RoseKafkaContainer container = new RoseKafkaContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseKafkaContainer.KAFKA_PORT)));
    }

}
