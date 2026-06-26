package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link HiveMqContainer}.
 */
class HiveMqContainerTests extends BaseDevServicesContainerTests<HiveMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        HiveMqContainer container = new HiveMqContainer(new MqttDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MqttDevServiceProperties properties = new MqttDevServiceProperties();
        properties.setPort(1234);

        HiveMqContainer container = new HiveMqContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + HiveMqContainer.MQTT_PORT)));
    }

}
