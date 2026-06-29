package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.testcontainers.containers.wait.strategy.Wait;

import io.zhijun.devservice.core.container.AbstractDevServiceContainer;

/**
 * HiveMQ CE container configured for Rose DevService.
 */
final class HiveMqContainer extends AbstractDevServiceContainer<HiveMqContainer, MqttDevServiceProperties> {

    static final String COMPATIBLE_IMAGE_NAME =
            org.testcontainers.utility.DockerImageName.parse(MqttDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int MQTT_PORT = 1883;

    HiveMqContainer(MqttDevServiceProperties properties) {
        super(properties, MqttDevServiceProperties.DEFAULT_IMAGE_NAME, MQTT_PORT);
        waitingFor(Wait.forLogMessage("(.*)Started HiveMQ in(.*)", 1));
    }

    int getMqttPort() {
        return getMappedDefaultPort();
    }

    String getBrokerUrl() {
        return getConnectionUrl("tcp");
    }

    @Override
    protected void configure() {
        super.configure();
    }
}
