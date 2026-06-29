package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;

/**
 * HiveMQ CE container configured for Rose DevService.
 */
final class HiveMqContainer extends GenericContainer<HiveMqContainer> {

    static final String COMPATIBLE_IMAGE_NAME =
            DockerImageName.parse(MqttDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int MQTT_PORT = 1883;

    private final MqttDevServiceProperties properties;

    HiveMqContainer(MqttDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(MQTT_PORT);
        waitingFor(Wait.forLogMessage("(.*)Started HiveMQ in(.*)", 1));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), MQTT_PORT);
        }
    }

    int getMqttPort() {
        return getMappedPort(MQTT_PORT);
    }

    String getBrokerUrl() {
        return "tcp://" + getHost() + ":" + getMqttPort();
    }
}
