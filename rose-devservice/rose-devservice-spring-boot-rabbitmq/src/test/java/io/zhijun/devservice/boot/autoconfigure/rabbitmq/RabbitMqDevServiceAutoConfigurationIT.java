package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.RabbitMQContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RabbitMqDevServicesAutoConfiguration}.
 */
class RabbitMqDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner =
            defaultContextRunner(RabbitMqDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return RabbitMqDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RabbitMqContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "rabbitmq";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                RabbitMQContainer.class, RabbitMqContainer.COMPATIBLE_IMAGE_NAME, container -> {
                    assertThat(container.getEnv()).isEmpty();
                });
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RabbitMqContainer.class, commonConfigurationProperties(), (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("spring.rabbitmq.host"))
                            .isNotBlank();
                    assertThat(context.getEnvironment().getProperty("spring.rabbitmq.port"))
                            .isNotBlank();
                });
    }
}
