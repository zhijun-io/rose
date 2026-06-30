package io.zhijun.devservice.boot.autoconfigure.mqtt;

import io.zhijun.devservice.core.docker.DockerEnvironmentSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link HiveMqContainer}.
 */
class HiveMqContainerIT {

    static {
        DockerEnvironmentSupport.configureIfNeeded();
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
