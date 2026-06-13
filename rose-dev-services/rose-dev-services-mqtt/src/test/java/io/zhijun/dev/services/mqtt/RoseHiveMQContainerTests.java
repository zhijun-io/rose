package io.zhijun.dev.services.mqtt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        MqttDevServicesProperties properties = new MqttDevServicesProperties();
        properties.setPort(1234);

        RoseHiveMQContainer container = new RoseHiveMQContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseHiveMQContainer.MQTT_PORT));
    }

    @Test
    void exposesBrokerUrlWhenStarted() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttDevServicesProperties());
        container.start();
        try {
            assertThat(container.getBrokerUrl()).startsWith("tcp://");
        } finally {
            container.stop();
        }
    }

}
