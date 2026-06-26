package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OnMicrometerMetricsBridgeEnabledCondition}.
 */
class OnMicrometerMetricsBridgeEnabledConditionTests {

    private final OnMicrometerMetricsBridgeEnabledCondition condition = new OnMicrometerMetricsBridgeEnabledCondition();

    private final MockEnvironment environment = new MockEnvironment();

    private final ConditionContext context = mock(ConditionContext.class);

    private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    @Test
    void doesNotMatchWhenOtlpMicrometerEnabled() {
        environment.setProperty(OpenTelemetryExporterProperties.MICROMETER_REGISTRY_ENABLED_PROPERTY, "true");
        when(context.getEnvironment()).thenReturn(environment);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains(OpenTelemetryExporterProperties.MICROMETER_REGISTRY_ENABLED_PROPERTY + " is true");
    }

    @Test
    void matchesWhenConsoleMetricsExporterEnabled() {
        environment.setProperty(OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY, "console");
        when(context.getEnvironment()).thenReturn(environment);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage()).contains("console or otlp metrics exporter enabled");
    }

    @Test
    void matchesWhenDefaultOtlpExporterEnabled() {
        when(context.getEnvironment()).thenReturn(environment);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
    }

    @Test
    void doesNotMatchWhenExporterDisabled() {
        environment.setProperty(OpenTelemetryExporterProperties.TYPE_PROPERTY, "none");
        environment.setProperty(OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY, "none");
        when(context.getEnvironment()).thenReturn(environment);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("no supported metrics exporter enabled");
    }

}
