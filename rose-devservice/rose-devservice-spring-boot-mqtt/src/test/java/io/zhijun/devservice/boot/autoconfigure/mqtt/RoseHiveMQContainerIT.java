package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerIT {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void exposesBrokerUrlWhenStarted() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttDevServiceProperties());
        container.start();
        try {
            assertThat(container.getBrokerUrl()).startsWith("tcp://");
        }
        finally {
            container.stop();
        }
    }

}
