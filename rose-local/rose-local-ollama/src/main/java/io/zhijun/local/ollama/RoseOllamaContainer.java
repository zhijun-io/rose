package io.zhijun.local.ollama;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.local.core.container.ContainerConfigurer;
import io.zhijun.local.core.util.ContainerUtils;

/**
 * Ollama container configured for Rose Local.
 */
final class RoseOllamaContainer extends GenericContainer<RoseOllamaContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "ollama/ollama";

    static final int OLLAMA_PORT = 11434;

    private final OllamaLocalServiceProperties properties;

    RoseOllamaContainer(OllamaLocalServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(OLLAMA_PORT);
        waitingFor(Wait.forHttp("/").forPort(OLLAMA_PORT).forStatusCodeMatching(status -> status < 500));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), OLLAMA_PORT);
        }
    }

    String getBaseUrl() {
        return "http://" + getHost() + ":" + getMappedPort(OLLAMA_PORT);
    }
}
