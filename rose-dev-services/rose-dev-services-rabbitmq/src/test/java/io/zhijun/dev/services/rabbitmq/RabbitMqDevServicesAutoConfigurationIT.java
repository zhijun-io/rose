package io.zhijun.dev.services.rabbitmq;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.RabbitMQContainer;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RabbitMqDevServicesAutoConfiguration}.
 */
class RabbitMqDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
            RabbitMqDevServicesAutoConfiguration.class);

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
        return RabbitMQContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "rabbitmq";
    }

    @Test
    void containerAvailableInDevMode() {
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(getContainerClass());
                    RabbitMQContainer container = context.getBean(RabbitMQContainer.class);
                    assertThat(container.getDockerImageName()).contains(RoseRabbitMqContainer.COMPATIBLE_IMAGE_NAME);
                    assertThat(container.getEnv()).isEmpty();
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();

                    assertThatHasSingletonScope(context);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(commonConfigurationProperties());

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    RabbitMQContainer container = context.getBean(RabbitMQContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    container.stop();
                });
    }
}
