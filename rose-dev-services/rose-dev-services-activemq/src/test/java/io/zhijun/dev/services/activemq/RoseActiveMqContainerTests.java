package io.zhijun.dev.services.activemq;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseActiveMqContainer}.
 */
class RoseActiveMqContainerTests extends BaseDevServicesContainerTests<RoseActiveMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseActiveMqContainer container = new RoseActiveMqContainer(new ActiveMqDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        ActiveMqDevServicesProperties properties = new ActiveMqDevServicesProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseActiveMqContainer container = new RoseActiveMqContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseActiveMqContainer.OPENWIRE_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseActiveMqContainer.WEB_CONSOLE_PORT)));
    }

}
