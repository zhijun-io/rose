package io.zhijun.dev.mysql;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseMySqlContainer}.
 */
class RoseMySqlContainerTests extends BaseDevServicesContainerTests<RoseMySqlContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMySqlContainer container = new RoseMySqlContainer(new MySqlDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MySqlDevServiceProperties properties = new MySqlDevServiceProperties();
        properties.setPort(1234);

        RoseMySqlContainer container = new RoseMySqlContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMySqlContainer.MYSQL_PORT)));
    }

}
