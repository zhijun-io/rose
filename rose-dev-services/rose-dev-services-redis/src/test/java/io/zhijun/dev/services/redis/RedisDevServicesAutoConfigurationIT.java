package io.zhijun.dev.services.redis;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RedisDevServicesAutoConfiguration}.
 */
class RedisDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(RedisDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return RedisDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RoseRedisContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "redis";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        getContextRunner().run(context -> {
            assertThat(context).hasSingleBean(getContainerClass());
            RoseRedisContainer container = context.getBean(RoseRedisContainer.class);
            assertThat(container.getDockerImageName()).contains(RoseRedisContainer.COMPATIBLE_IMAGE_NAME);
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
                    RoseRedisContainer container = context.getBean(RoseRedisContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(context.getEnvironment().getProperty("spring.redis.host")).isNotBlank();
                    assertThat(context.getEnvironment().getProperty("spring.redis.port")).isNotBlank();
                    container.stop();
                });
    }
}
