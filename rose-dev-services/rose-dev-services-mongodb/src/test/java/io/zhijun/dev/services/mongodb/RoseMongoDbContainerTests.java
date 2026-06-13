package io.zhijun.dev.services.mongodb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseMongoDbContainer}.
 */
class RoseMongoDbContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseMongoDbContainer container = new RoseMongoDbContainer(new MongoDbDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        MongoDbDevServicesProperties properties = new MongoDbDevServicesProperties();
        properties.setPort(1234);

        RoseMongoDbContainer container = new RoseMongoDbContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseMongoDbContainer.MONGODB_PORT));
    }

}
