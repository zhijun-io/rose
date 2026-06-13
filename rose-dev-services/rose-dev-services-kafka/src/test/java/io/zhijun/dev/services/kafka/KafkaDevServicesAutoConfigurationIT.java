package io.zhijun.dev.services.kafka;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.KafkaContainer;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KafkaDevServicesAutoConfiguration}.
 */
class KafkaDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
            KafkaDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return KafkaDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return KafkaContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "kafka";
    }

    @Test
    void containerAvailableInDevMode() {
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(KafkaContainer.class);
                    KafkaContainer container = context.getBean(KafkaContainer.class);
                    assertThat(container.getDockerImageName()).contains("confluentinc/cp-kafka");
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();
                    assertThat(container.getBinds()).isEmpty();

                    assertThatHasSingletonScope(context);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(commonConfigurationProperties());

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    KafkaContainer container = context.getBean(KafkaContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    container.stop();
                });
    }
}
