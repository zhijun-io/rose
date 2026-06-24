package io.zhijun.observation.boot.autoconfigure.conventions.otel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.core.TelemetryConventionsBackend;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryConventionsAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryConventionsAutoConfiguration.class));

    @Test
    void registersTelemetryConventionsBackend() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TelemetryConventionsBackend.class);
            assertThat(context.getBean(TelemetryConventionsBackend.class).id()).isEqualTo("opentelemetry");
            assertThat(context.getBean(TelemetryConventionsBackend.class).defaultCandidate()).isTrue();
        });
    }
}
