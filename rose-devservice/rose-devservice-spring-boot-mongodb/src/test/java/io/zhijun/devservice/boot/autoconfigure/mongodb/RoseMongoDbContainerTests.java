package io.zhijun.devservice.boot.autoconfigure.mongodb;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseMongoDbContainer}.
 */
class RoseMongoDbContainerTests extends BaseDevServicesContainerTests<RoseMongoDbContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMongoDbContainer container = new RoseMongoDbContainer(new MongoDbDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MongoDbDevServiceProperties properties = new MongoDbDevServiceProperties();
        properties.setPort(1234);

        RoseMongoDbContainer container = new RoseMongoDbContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMongoDbContainer.MONGODB_PORT)));
    }

}
