package io.zhijun.dev.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.command.InspectContainerResponse;

import io.zhijun.dev.core.container.ContainerConfigurer;
import io.zhijun.dev.core.util.ContainerUtils;

/**
 * RabbitMQ container configured for Rose DevSrevice.
 */
final class RoseRabbitMqContainer extends RabbitMQContainer {

    private static final Logger logger = LoggerFactory.getLogger(RoseRabbitMqContainer.class);

    static final String COMPATIBLE_IMAGE_NAME = "rabbitmq";

    static final int AMQP_PORT = 5672;

    static final int HTTP_PORT = 15672;

    private final RabbitMqDevServiceProperties properties;

    RoseRabbitMqContainer(RabbitMqDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), AMQP_PORT);
        }
        if (ContainerUtils.isValidPort(properties.getManagementConsolePort())) {
            addFixedExposedPort(properties.getManagementConsolePort(), HTTP_PORT);
        }
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        logger.info("RabbitMQ Management Console: {}", getHttpUrl());
    }
}
