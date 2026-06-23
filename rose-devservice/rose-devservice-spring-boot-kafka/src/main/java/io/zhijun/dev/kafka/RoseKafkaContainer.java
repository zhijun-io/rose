package io.zhijun.dev.kafka;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.dev.core.container.ContainerConfigurer;
import io.zhijun.dev.core.util.ContainerUtils;

/**
 * Kafka container configured for Rose DevService.
 */
final class RoseKafkaContainer extends KafkaContainer {

    static final String COMPATIBLE_IMAGE_NAME = "confluentinc/cp-kafka";

    static final int KAFKA_PORT = 9093;

    private final KafkaDevServiceProperties properties;

    RoseKafkaContainer(KafkaDevServiceProperties properties) {
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
