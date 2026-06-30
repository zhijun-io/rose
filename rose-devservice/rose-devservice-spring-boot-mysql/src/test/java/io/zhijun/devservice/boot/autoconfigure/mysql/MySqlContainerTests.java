package io.zhijun.devservice.boot.autoconfigure.mysql;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MySqlContainer}.
 */
class MySqlContainerTests extends BaseDevServicesContainerTests<MySqlContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        MySqlContainer container = new MySqlContainer(new MySqlDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MySqlDevServiceProperties properties = new MySqlDevServiceProperties();
        properties.setPort(1234);

        MySqlContainer container = new MySqlContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(
                                binding -> binding.startsWith(properties.getPort() + ":" + MySqlContainer.MYSQL_PORT)));
    }
}
