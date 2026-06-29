package io.zhijun.devservice.boot.autoconfigure.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.DockerTestSupport;

/**
 * Integration test for {@link HiveMqContainer}.
 */
class HiveMqContainerIT {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void exposesBrokerUrlWhenStarted() {
        HiveMqContainer container = new HiveMqContainer(new MqttDevServiceProperties());
        container.start();
        try {
            assertThat(container.getBrokerUrl()).startsWith("tcp://");
        } finally {
            container.stop();
        }
    }
}
