package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link KafkaDevServicesAutoConfiguration}.
 */
class KafkaDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner =
            defaultContextRunner(KafkaDevServicesAutoConfiguration.class);

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
        return DevServiceKafkaContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "kafka";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                ConfluentKafkaContainer.class,
                "confluentinc/cp-kafka",
                container -> assertThat(container.getBinds()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                DevServiceKafkaContainer.class,
                commonConfigurationProperties(),
                (context, container) -> assertThat(
                                context.getEnvironment().getProperty("spring.kafka.bootstrap-servers"))
                        .isNotBlank());
    }
}
