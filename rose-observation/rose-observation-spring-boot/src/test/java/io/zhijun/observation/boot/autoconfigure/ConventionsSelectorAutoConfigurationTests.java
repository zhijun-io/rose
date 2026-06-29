package io.zhijun.observation.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.conventions.TelemetryConventionsBackend;

class ConventionsSelectorAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConventionsSelectorAutoConfiguration.class));

    @Test
    void whenSingleBackendThenSelectsIt() {
        contextRunner
                .withBean(
                        "opentelemetry",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry", true))
                .run(context -> assertThat(context.getBean(TelemetryConventionsBackend.class)
                                .id())
                        .isEqualTo("opentelemetry"));
    }

    @Test
    void whenMultipleBackendsThenThrowsUnlessConfigured() {
        contextRunner
                .withBean(
                        "backend1",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("openinference"))
                .withBean(
                        "backend2",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry"))
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(AmbiguousConventionsBackendException.class));
    }

    @Test
    void whenSingleDefaultCandidateThenSelectsIt() {
        contextRunner
                .withBean(
                        "backend1",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("openinference"))
                .withBean(
                        "backend2",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry", true))
                .run(context -> assertThat(context.getBean(TelemetryConventionsBackend.class)
                                .id())
                        .isEqualTo("opentelemetry"));
    }

    @Test
    void whenBackendConfiguredThenSelectsMatchingBackend() {
        contextRunner
                .withPropertyValues("rose.observation.conventions.backend=opentelemetry")
                .withBean(
                        "backend1",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("openinference"))
                .withBean(
                        "backend2",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry"))
                .run(context -> assertThat(context.getBean(TelemetryConventionsBackend.class)
                                .id())
                        .isEqualTo("opentelemetry"));
    }

    @Test
    void whenConfiguredBackendMissingThenFails() {
        contextRunner
                .withPropertyValues("rose.observation.conventions.backend=unknown")
                .withBean(
                        "backend1",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("openinference"))
                .withBean(
                        "backend2",
                        TelemetryConventionsBackend.class,
                        () -> TelemetryConventionsBackend.of("opentelemetry"))
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(UnknownConventionsBackendException.class));
    }
}
