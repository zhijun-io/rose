package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterConditionSupport;

/**
 * Determines if a certain exporter type is enabled for OpenTelemetry traces.
 */
class OnOpenTelemetryTracingExporterCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return OpenTelemetryExporterConditionSupport.evaluate(context, metadata,
                ConditionalOnOpenTelemetryTracingExporter.class,
                OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".type");
    }

}
