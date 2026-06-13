package io.zhijun.dev.services.mqtt;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MqttDevServicesAutoConfiguration}.
 */
class MqttDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

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
        getContextRunner().run(context -> {
            assertThat(context).hasSingleBean(getContainerClass());
            RoseHiveMQContainer container = context.getBean(RoseHiveMQContainer.class);
            assertThat(container.getDockerImageName()).contains(RoseHiveMQContainer.COMPATIBLE_IMAGE_NAME);
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
                    RoseHiveMQContainer container = context.getBean(RoseHiveMQContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(context.getEnvironment().getProperty("mqtt.server.uri")).startsWith("tcp://");
                    assertThat(context.getEnvironment().getProperty("spring.mqtt.url")).startsWith("tcp://");
                    container.stop();
                });
    }
}
