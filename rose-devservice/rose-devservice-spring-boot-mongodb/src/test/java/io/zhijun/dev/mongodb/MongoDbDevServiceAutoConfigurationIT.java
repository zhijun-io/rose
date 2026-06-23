package io.zhijun.dev.mongodb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.MongoDBContainer;

import io.zhijun.dev.test.BaseDevServiceAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link MongoDbDevServicesAutoConfiguration}.
 */
class MongoDbDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

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
        return RoseMongoDbContainer.class;
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
        assertContainerConfigurationApplied(
                RoseMongoDbContainer.class,
                commonConfigurationProperties(),
                (context, container) -> assertThat(
                        context.getEnvironment().getProperty("spring.data.mongodb.uri")).isNotBlank());
    }
}
