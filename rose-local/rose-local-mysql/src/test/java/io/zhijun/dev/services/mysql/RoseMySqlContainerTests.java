package io.zhijun.dev.services.mysql;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseMySqlContainer}.
 */
class RoseMySqlContainerTests extends BaseDevServicesContainerTests<RoseMySqlContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMySqlContainer container = new RoseMySqlContainer(new MySqlDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MySqlDevServicesProperties properties = new MySqlDevServicesProperties();
        properties.setPort(1234);

        RoseMySqlContainer container = new RoseMySqlContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMySqlContainer.MYSQL_PORT)));
    }

}
