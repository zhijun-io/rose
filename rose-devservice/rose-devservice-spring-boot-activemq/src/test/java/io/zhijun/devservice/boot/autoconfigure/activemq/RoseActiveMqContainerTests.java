package io.zhijun.devservice.boot.autoconfigure.activemq;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseActiveMqContainer}.
 */
class RoseActiveMqContainerTests extends BaseDevServicesContainerTests<RoseActiveMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseActiveMqContainer container = new RoseActiveMqContainer(new ActiveMqDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        ActiveMqDevServiceProperties properties = new ActiveMqDevServiceProperties();
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
