package io.zhijun.devservice.boot.autoconfigure.artemis;

import com.github.dockerjava.api.command.InspectContainerResponse;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * ActiveMQ Artemis container for Rose DevService.
 */
final class ArtemisContainer extends GenericContainer<ArtemisContainer> {

    private static final Logger logger = LoggerFactory.getLogger(ArtemisContainer.class);

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(ArtemisDevServiceProperties.DEFAULT_IMAGE_NAME)
            .getUnversionedPart();

    static final int TCP_PORT = 61616;

    static final int WEB_CONSOLE_PORT = 8161;

    private final ArtemisDevServiceProperties properties;

    ArtemisContainer(ArtemisDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(TCP_PORT, WEB_CONSOLE_PORT);
        waitingFor(Wait.forLogMessage(".*AMQ221007: Server is now live.*", 1));

        withEnv("AMQ_USER", properties.getUsername());
        withEnv("AMQ_PASSWORD", properties.getPassword());

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), TCP_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getManagementConsolePort())) {
            addFixedExposedPort(properties.getManagementConsolePort(), WEB_CONSOLE_PORT);
        }
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        logger.info("Artemis Management Console: {}", getManagementConsoleUrl());
    }

    String getBrokerUrl() {
        return "tcp://" + getHost() + ":" + getMappedPort(TCP_PORT);
    }

    String getManagementConsoleUrl() {
        return "http://" + getHost() + ":" + getMappedPort(WEB_CONSOLE_PORT) + "/console";
    }

    String getUsername() {
        return properties.getUsername();
    }

    String getPassword() {
        return properties.getPassword();
    }
}
