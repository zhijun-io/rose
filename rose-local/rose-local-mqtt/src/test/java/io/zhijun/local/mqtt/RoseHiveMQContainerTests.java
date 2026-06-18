package io.zhijun.local.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerTests extends BaseDevServicesContainerTests<RoseHiveMQContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MqttLocalServiceProperties properties = new MqttLocalServiceProperties();
        properties.setPort(1234);

        RoseHiveMQContainer container = new RoseHiveMQContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseHiveMQContainer.MQTT_PORT)));
    }

}
