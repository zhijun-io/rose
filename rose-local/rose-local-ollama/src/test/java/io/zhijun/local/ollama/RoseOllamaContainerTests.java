package io.zhijun.local.ollama;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseOllamaContainer}.
 */
class RoseOllamaContainerTests extends BaseDevServicesContainerTests<RoseOllamaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOllamaContainer container = new RoseOllamaContainer(new OllamaLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OllamaLocalServiceProperties properties = new OllamaLocalServiceProperties();
        properties.setPort(1234);

        RoseOllamaContainer container = new RoseOllamaContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseOllamaContainer.OLLAMA_PORT)));
    }

}
