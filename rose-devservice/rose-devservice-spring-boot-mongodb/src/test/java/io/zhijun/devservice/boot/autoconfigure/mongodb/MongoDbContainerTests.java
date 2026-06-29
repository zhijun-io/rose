package io.zhijun.devservice.boot.autoconfigure.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

/**
 * Unit test for {@link MongoDbContainer}.
 */
class MongoDbContainerTests extends BaseDevServicesContainerTests<MongoDbContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        MongoDbContainer container = new MongoDbContainer(new MongoDbDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        MongoDbDevServiceProperties properties = new MongoDbDevServiceProperties();
        properties.setPort(1234);

        MongoDbContainer container = new MongoDbContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(binding ->
                                binding.startsWith(properties.getPort() + ":" + MongoDbContainer.MONGODB_PORT)));
    }
}
