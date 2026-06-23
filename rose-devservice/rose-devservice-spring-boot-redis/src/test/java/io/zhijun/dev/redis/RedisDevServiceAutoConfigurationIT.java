package io.zhijun.dev.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.test.BaseDevServiceAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RedisDevServicesAutoConfiguration}.
 */
class RedisDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

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
        assertContainerAvailableWithDefaultConfiguration(
                RoseRedisContainer.class,
                RoseRedisContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseRedisContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("spring.redis.host")).isNotBlank();
                    assertThat(context.getEnvironment().getProperty("spring.redis.port")).isNotBlank();
                });
    }
}
