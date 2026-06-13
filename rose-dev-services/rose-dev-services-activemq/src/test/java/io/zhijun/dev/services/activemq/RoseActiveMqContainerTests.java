package io.zhijun.dev.services.activemq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseActiveMqContainer}.
 */
class RoseActiveMqContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseActiveMqContainer container = new RoseActiveMqContainer(new ActiveMqDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        ActiveMqDevServicesProperties properties = new ActiveMqDevServicesProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseActiveMqContainer container = new RoseActiveMqContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseActiveMqContainer.OPENWIRE_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseActiveMqContainer.WEB_CONSOLE_PORT));
    }

}
