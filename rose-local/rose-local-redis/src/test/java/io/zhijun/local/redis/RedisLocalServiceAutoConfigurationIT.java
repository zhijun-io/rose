package io.zhijun.local.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.local.tests.BaseLocalServiceAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RedisDevServicesAutoConfiguration}.
 */
class RedisLocalServiceAutoConfigurationIT extends BaseLocalServiceAutoConfigurationIT {

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
