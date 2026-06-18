package io.zhijun.local.mqtt;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RoseHiveMQContainer}.
 */
class RoseHiveMQContainerIT {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void exposesBrokerUrlWhenStarted() {
        RoseHiveMQContainer container = new RoseHiveMQContainer(new MqttLocalServiceProperties());
        container.start();
        try {
            assertThat(container.getBrokerUrl()).startsWith("tcp://");
        }
        finally {
            container.stop();
        }
    }

}
