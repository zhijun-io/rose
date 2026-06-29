package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterConditionSupport;

/**
 * Determines if a certain exporter type is enabled for OpenTelemetry metrics.
 */
class OnOpenTelemetryMetricsExporterCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return OpenTelemetryExporterConditionSupport.evaluate(
                context,
                metadata,
                ConditionalOnOpenTelemetryMetricsExporter.class,
                OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY);
    }
}
