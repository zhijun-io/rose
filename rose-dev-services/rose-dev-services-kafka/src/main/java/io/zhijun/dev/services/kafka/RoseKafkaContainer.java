package io.zhijun.dev.services.kafka;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.dev.services.core.container.ContainerConfigurer;
import io.zhijun.dev.services.core.util.ContainerUtils;

/**
 * Kafka container configured for Rose Dev Services.
 */
final class RoseKafkaContainer extends KafkaContainer {

    static final String COMPATIBLE_IMAGE_NAME = "confluentinc/cp-kafka";

    static final int KAFKA_PORT = 9093;

    private final KafkaDevServicesProperties properties;

    RoseKafkaContainer(KafkaDevServicesProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), KAFKA_PORT);
        }
    }
}
