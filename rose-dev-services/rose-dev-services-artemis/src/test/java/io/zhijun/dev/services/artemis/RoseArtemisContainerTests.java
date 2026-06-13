package io.zhijun.dev.services.artemis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseArtemisContainer}.
 */
class RoseArtemisContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseArtemisContainer container = new RoseArtemisContainer(new ArtemisDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        ArtemisDevServicesProperties properties = new ArtemisDevServicesProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        RoseArtemisContainer container = new RoseArtemisContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseArtemisContainer.TCP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getManagementConsolePort() + ":" + RoseArtemisContainer.WEB_CONSOLE_PORT));
    }

}
