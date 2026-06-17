package io.zhijun.dev.services.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerTests extends BaseDevServicesContainerTests<RoseHiveMQContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MqttDevServicesProperties properties = new MqttDevServicesProperties();
        properties.setPort(1234);

        RoseHiveMQContainer container = new RoseHiveMQContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseHiveMQContainer.MQTT_PORT)));
    }

}
