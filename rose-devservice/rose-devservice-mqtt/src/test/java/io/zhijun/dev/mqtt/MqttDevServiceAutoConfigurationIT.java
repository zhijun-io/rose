package io.zhijun.dev.mqtt;

import io.zhijun.dev.tests.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MqttDevServicesAutoConfiguration}.
 */
class MqttDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(MqttDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return MqttDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RoseHiveMQContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "mqtt";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        assertContainerAvailableWithDefaultConfiguration(
                RoseHiveMQContainer.class,
                RoseHiveMQContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseHiveMQContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("mqtt.server.uri")).startsWith("tcp://");
                    assertThat(context.getEnvironment().getProperty("spring.mqtt.url")).startsWith("tcp://");
                });
    }
}
