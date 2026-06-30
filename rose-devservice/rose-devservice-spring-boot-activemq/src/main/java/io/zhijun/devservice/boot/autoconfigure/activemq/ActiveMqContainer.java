package io.zhijun.devservice.boot.autoconfigure.activemq;

import com.github.dockerjava.api.command.InspectContainerResponse;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * ActiveMQ Classic container for Rose DevService.
 */
final class ActiveMqContainer extends GenericContainer<ActiveMqContainer> {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMqContainer.class);

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(ActiveMqDevServiceProperties.DEFAULT_IMAGE_NAME)
            .getUnversionedPart();

    static final int OPENWIRE_PORT = 61616;

    static final int WEB_CONSOLE_PORT = 8161;

    private final ActiveMqDevServiceProperties properties;

    ActiveMqContainer(ActiveMqDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(OPENWIRE_PORT, WEB_CONSOLE_PORT);
        waitingFor(Wait.forLogMessage(".*Apache ActiveMQ.* started.*", 1));

        withEnv("ACTIVEMQ_CONNECTION_USER", properties.getUsername());
        withEnv("ACTIVEMQ_CONNECTION_PASSWORD", properties.getPassword());

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), OPENWIRE_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getManagementConsolePort())) {
            addFixedExposedPort(properties.getManagementConsolePort(), WEB_CONSOLE_PORT);
        }
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        logger.info("ActiveMQ Management Console: {}", getManagementConsoleUrl());
    }

    String getBrokerUrl() {
        return "tcp://" + getHost() + ":" + getMappedPort(OPENWIRE_PORT);
    }

    String getManagementConsoleUrl() {
        return "http://" + getHost() + ":" + getMappedPort(WEB_CONSOLE_PORT) + "/admin";
    }

    String getUsername() {
        return properties.getUsername();
    }

    String getPassword() {
        return properties.getPassword();
    }
}
