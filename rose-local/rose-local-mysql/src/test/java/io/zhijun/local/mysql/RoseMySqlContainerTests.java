package io.zhijun.local.mysql;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseMySqlContainer}.
 */
class RoseMySqlContainerTests extends BaseDevServicesContainerTests<RoseMySqlContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMySqlContainer container = new RoseMySqlContainer(new MySqlLocalServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MySqlLocalServiceProperties properties = new MySqlLocalServiceProperties();
        properties.setPort(1234);

        RoseMySqlContainer container = new RoseMySqlContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMySqlContainer.MYSQL_PORT)));
    }

}
