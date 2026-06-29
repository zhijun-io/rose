package io.zhijun.devservice.boot.autoconfigure.activemq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

/**
 * Unit test for {@link ActiveMqContainer}.
 */
class ActiveMqContainerTests extends BaseDevServicesContainerTests<ActiveMqContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        ActiveMqContainer container = new ActiveMqContainer(new ActiveMqDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        ActiveMqDevServiceProperties properties = new ActiveMqDevServiceProperties();
        properties.setPort(1234);
        properties.setManagementConsolePort(5678);

        ActiveMqContainer container = new ActiveMqContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(binding ->
                                binding.startsWith(properties.getPort() + ":" + ActiveMqContainer.OPENWIRE_PORT))
                        .anyMatch(binding -> binding.startsWith(
                                properties.getManagementConsolePort() + ":" + ActiveMqContainer.WEB_CONSOLE_PORT)));
    }
}
