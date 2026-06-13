package io.zhijun.dev.services.ollama;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseOllamaContainer}.
 */
class RoseOllamaContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOllamaContainer container = new RoseOllamaContainer(new OllamaDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        OllamaDevServicesProperties properties = new OllamaDevServicesProperties();
        properties.setPort(1234);

        RoseOllamaContainer container = new RoseOllamaContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseOllamaContainer.OLLAMA_PORT));
    }

}
