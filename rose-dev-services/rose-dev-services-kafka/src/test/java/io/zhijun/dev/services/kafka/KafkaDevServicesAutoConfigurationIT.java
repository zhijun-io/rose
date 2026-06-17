package io.zhijun.dev.services.kafka;

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
        assertContainerAvailableInDevMode(KafkaContainer.class, "confluentinc/cp-kafka", container ->
                assertThat(container.getBinds()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationDeclared(KafkaContainer.class, commonConfigurationProperties(), container -> {
        });
    }
}
