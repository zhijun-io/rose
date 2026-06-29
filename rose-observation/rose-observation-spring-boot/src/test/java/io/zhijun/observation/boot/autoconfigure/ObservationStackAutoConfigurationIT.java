package io.zhijun.observation.boot.autoconfigure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.conventions.TelemetryConventionsBackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the default Rose observation Boot stack entry points.
 */
class ObservationStackAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConventionsSelectorAutoConfiguration.class,
                    OpenTelemetryAutoConfiguration.class));

    @Test
    void shouldStartObservationDomainAndOtelSdk() {
        contextRunner
                .withBean("opentelemetry", TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry", true))
                .withPropertyValues("rose.otel.enabled=true")
                .run(context -> {
                    assertThat(context.getBean(TelemetryConventionsBackend.class).id()).isEqualTo("opentelemetry");
                    assertThat(context).hasSingleBean(ObservationProperties.class);
                    assertThat(context.getBean(OpenTelemetry.class)).isInstanceOf(OpenTelemetrySdk.class);
                });
    }

    @Test
    void shouldUseNoopOpenTelemetryWhenDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.enabled=false")
                .run(context -> assertThat(context.getBean(OpenTelemetry.class)).isSameAs(OpenTelemetry.noop()));
    }

}
