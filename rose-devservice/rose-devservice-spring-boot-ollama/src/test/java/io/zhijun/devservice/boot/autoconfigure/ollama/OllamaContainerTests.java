package io.zhijun.devservice.boot.autoconfigure.ollama;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OllamaContainer}.
 */
class OllamaContainerTests extends BaseDevServicesContainerTests<OllamaContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        OllamaContainer container = new OllamaContainer(new OllamaDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OllamaDevServiceProperties properties = new OllamaDevServiceProperties();
        properties.setPort(1234);

        OllamaContainer container = new OllamaContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + OllamaContainer.OLLAMA_PORT)));
    }

}
