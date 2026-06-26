package io.zhijun.devservice.boot.autoconfigure.mqtt;

import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link MqttDevServicesAutoConfiguration}.
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
        return HiveMqContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "mqtt";
    }

    @Test
    void containerAvailableWithDefaultConfiguration() {
        assertContainerAvailableWithDefaultConfiguration(
                HiveMqContainer.class,
                HiveMqContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                HiveMqContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("mqtt.server.uri")).startsWith("tcp://");
                    assertThat(context.getEnvironment().getProperty("spring.mqtt.url")).startsWith("tcp://");
                });
    }
}
