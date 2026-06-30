package io.zhijun.devservice.boot.autoconfigure.artemis;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ArtemisContainer}.
 */
class ArtemisContainerTests extends BaseDevServicesContainerTests<ArtemisContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        ArtemisContainer container = new ArtemisContainer(new ArtemisDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        ArtemisDevServiceProperties properties = new ArtemisDevServiceProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        ArtemisContainer container = new ArtemisContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(binding -> binding.startsWith(properties.getPort() + ":" + ArtemisContainer.TCP_PORT))
                        .anyMatch(binding -> binding.startsWith(
                                properties.getManagementConsolePort() + ":" + ArtemisContainer.WEB_CONSOLE_PORT)));
    }
}
