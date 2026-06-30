package io.zhijun.devservice.boot.autoconfigure.ollama;

import io.zhijun.devservice.core.container.AbstractDevServiceContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Ollama container configured for Rose DevService.
 */
final class OllamaContainer extends AbstractDevServiceContainer<OllamaContainer, OllamaDevServiceProperties> {

    static final String COMPATIBLE_IMAGE_NAME =
            org.testcontainers.utility.DockerImageName.parse(OllamaDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int OLLAMA_PORT = 11434;

    OllamaContainer(OllamaDevServiceProperties properties) {
        super(properties, OllamaDevServiceProperties.DEFAULT_IMAGE_NAME, OLLAMA_PORT);
        waitingFor(Wait.forHttp("/").forPort(OLLAMA_PORT).forStatusCodeMatching(status -> status < 500));
    }

    String getBaseUrl() {
        return getConnectionUrl("http");
    }

    @Override
    protected void configure() {
        super.configure();
    }
}
