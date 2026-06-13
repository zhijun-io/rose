package io.zhijun.dev.services.mqtt;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.dev.services.core.container.ContainerConfigurer;
import io.zhijun.dev.services.core.util.ContainerUtils;

/**
 * HiveMQ CE container configured for Rose Dev Services.
 */
final class RoseHiveMQContainer extends GenericContainer<RoseHiveMQContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "hivemq/hivemq-ce";

    static final int MQTT_PORT = 1883;

    private final MqttDevServicesProperties properties;

    RoseHiveMQContainer(MqttDevServicesProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(MQTT_PORT);
        waitingFor(Wait.forLogMessage("(.*)Started HiveMQ in(.*)", 1));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (ContainerUtils.isValidPort(properties.getPort())) {
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
