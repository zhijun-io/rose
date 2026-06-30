package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Kafka container configured for Rose DevService.
 */
final class DevServiceKafkaContainer extends ConfluentKafkaContainer {

    static final String COMPATIBLE_IMAGE_NAME =
            DockerImageName.parse(KafkaDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int KAFKA_PORT = 9093;

    DevServiceKafkaContainer(KafkaDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));

        ContainerConfigurer.base(this, properties);
        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), KAFKA_PORT);
        }
    }
}
