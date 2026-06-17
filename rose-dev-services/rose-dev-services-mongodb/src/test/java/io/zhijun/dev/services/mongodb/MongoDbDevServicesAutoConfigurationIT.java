package io.zhijun.dev.services.mongodb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.MongoDBContainer;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MongoDbDevServicesAutoConfiguration}.
 */
class MongoDbDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
            MongoDbDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return MongoDbDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return MongoDBContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "mongodb";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        assertContainerAvailableWithDefaultConfiguration(
                MongoDBContainer.class,
                RoseMongoDbContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationDeclared(MongoDBContainer.class, commonConfigurationProperties(), container -> {
        });
    }
}
