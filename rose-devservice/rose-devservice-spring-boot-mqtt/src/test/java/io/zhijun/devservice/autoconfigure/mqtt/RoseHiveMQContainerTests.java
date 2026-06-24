package io.zhijun.devservice.autoconfigure.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerTests extends BaseDevServicesContainerTests<RoseHiveMQContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MqttDevServiceProperties properties = new MqttDevServiceProperties();
        properties.setPort(1234);

        RoseHiveMQContainer container = new RoseHiveMQContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseHiveMQContainer.MQTT_PORT)));
    }

}
