package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import com.github.dockerjava.api.command.InspectContainerResponse;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * RabbitMQ container configured for Rose DevService.
 */
final class RabbitMqContainer extends RabbitMQContainer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqContainer.class);

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(RabbitMqDevServiceProperties.DEFAULT_IMAGE_NAME)
            .getUnversionedPart();

    static final int AMQP_PORT = 5672;

    static final int HTTP_PORT = 15672;

    private final RabbitMqDevServiceProperties properties;

    RabbitMqContainer(RabbitMqDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), AMQP_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getManagementConsolePort())) {
            addFixedExposedPort(properties.getManagementConsolePort(), HTTP_PORT);
        }
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        logger.info("RabbitMQ Management Console: {}", getHttpUrl());
    }
}
