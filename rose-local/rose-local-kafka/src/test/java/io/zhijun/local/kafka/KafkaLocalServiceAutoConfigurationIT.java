package io.zhijun.local.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.KafkaContainer;

import io.zhijun.local.tests.BaseLocalServiceAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KafkaDevServicesAutoConfiguration}.
 */
class KafkaLocalServiceAutoConfigurationIT extends BaseLocalServiceAutoConfigurationIT {

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
        assertContainerAvailableInDevMode(KafkaContainer.class, "confluentinc/cp-kafka", container ->
                assertThat(container.getBinds()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseKafkaContainer.class,
                commonConfigurationProperties(),
                (context, container) -> assertThat(
                        context.getEnvironment().getProperty("spring.kafka.bootstrap-servers")).isNotBlank());
    }
}
