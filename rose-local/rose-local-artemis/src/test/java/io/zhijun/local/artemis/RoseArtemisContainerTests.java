package io.zhijun.local.artemis;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseArtemisContainer}.
 */
class RoseArtemisContainerTests extends BaseDevServicesContainerTests<RoseArtemisContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseArtemisContainer container = new RoseArtemisContainer(new ArtemisLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        ArtemisLocalServiceProperties properties = new ArtemisLocalServiceProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseArtemisContainer container = new RoseArtemisContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseArtemisContainer.TCP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseArtemisContainer.WEB_CONSOLE_PORT)));
    }

}
