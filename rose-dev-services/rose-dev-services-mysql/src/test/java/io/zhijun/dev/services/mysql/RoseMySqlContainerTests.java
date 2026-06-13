package io.zhijun.dev.services.mysql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseMySqlContainer}.
 */
class RoseMySqlContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMySqlContainer container = new RoseMySqlContainer(new MySqlDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        MySqlDevServicesProperties properties = new MySqlDevServicesProperties();
        properties.setPort(1234);

        RoseMySqlContainer container = new RoseMySqlContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMySqlContainer.MYSQL_PORT));
    }

}
