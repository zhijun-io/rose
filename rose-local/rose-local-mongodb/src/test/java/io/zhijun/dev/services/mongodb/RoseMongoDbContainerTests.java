package io.zhijun.dev.services.mongodb;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseMongoDbContainer}.
 */
class RoseMongoDbContainerTests extends BaseDevServicesContainerTests<RoseMongoDbContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMongoDbContainer container = new RoseMongoDbContainer(new MongoDbDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MongoDbDevServicesProperties properties = new MongoDbDevServicesProperties();
        properties.setPort(1234);

        RoseMongoDbContainer container = new RoseMongoDbContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMongoDbContainer.MONGODB_PORT)));
    }

}
