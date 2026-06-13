package io.zhijun.dev.services.mongodb;

import org.apache.commons.lang3.ArrayUtils;
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
        getContextRunner().run(context -> {
            assertThat(context).hasSingleBean(getContainerClass());
            MongoDBContainer container = (MongoDBContainer) context.getBean(getContainerClass());
            assertThat(container.getDockerImageName()).contains(RoseMongoDbContainer.COMPATIBLE_IMAGE_NAME);
            assertThat(container.getEnv()).isEmpty();
            assertThat(container.getNetworkAliases()).hasSize(1);
            assertThat(container.isShouldBeReused()).isFalse();

            assertThatHasSingletonScope(context);
        });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(commonConfigurationProperties());

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    MongoDBContainer container = (MongoDBContainer) context.getBean(getContainerClass());
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    container.stop();
                });
    }
}
