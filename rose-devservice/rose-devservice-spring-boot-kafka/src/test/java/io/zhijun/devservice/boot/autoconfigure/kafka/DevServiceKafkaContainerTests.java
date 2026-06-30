package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DevServiceKafkaContainer}.
 */
class DevServiceKafkaContainerTests extends BaseDevServicesContainerTests<DevServiceKafkaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        DevServiceKafkaContainer container = new DevServiceKafkaContainer(new KafkaDevServiceProperties());
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        KafkaDevServiceProperties properties = new KafkaDevServiceProperties();
        properties.setPort(1234);

        DevServiceKafkaContainer container = new DevServiceKafkaContainer(properties);
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(binding ->
                                binding.startsWith(properties.getPort() + ":" + DevServiceKafkaContainer.KAFKA_PORT)));
    }
}
