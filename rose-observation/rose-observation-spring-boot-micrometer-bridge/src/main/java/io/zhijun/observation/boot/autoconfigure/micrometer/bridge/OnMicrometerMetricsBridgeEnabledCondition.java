package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterTypeNames;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

/**
 * Activates the Micrometer metrics bridge when OTLP Micrometer registry export is disabled
 * and either console or OTLP metrics export is enabled.
 */
class OnMicrometerMetricsBridgeEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean otlpMicrometerEnabled = context.getEnvironment()
                .getProperty(
                        OpenTelemetryExporterProperties.MICROMETER_REGISTRY_ENABLED_PROPERTY, Boolean.class, false);
        if (otlpMicrometerEnabled) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(
                            getClass().getName())
                    .because(OpenTelemetryExporterProperties.MICROMETER_REGISTRY_ENABLED_PROPERTY + " is true"));
        }

        if (isMetricsExporterEnabled(context, ExporterTypeNames.CONSOLE)
                || isMetricsExporterEnabled(context, ExporterTypeNames.OTLP)) {
            return ConditionOutcome.match(
                    ConditionMessage.forCondition(getClass().getName())
                            .because("console or otlp metrics exporter enabled"));
        }

        return ConditionOutcome.noMatch(
                ConditionMessage.forCondition(getClass().getName()).because("no supported metrics exporter enabled"));
    }

    private static boolean isMetricsExporterEnabled(ConditionContext context, String requestedExporterType) {
        String metricsExporterTypeString = context.getEnvironment()
                .getProperty(OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY, String.class);
        if (StringUtils.hasText(metricsExporterTypeString)) {
            return metricsExporterTypeString.equalsIgnoreCase(requestedExporterType);
        }

        String generalExporterTypeString = context.getEnvironment()
                .getProperty(OpenTelemetryExporterProperties.TYPE_PROPERTY, ExporterTypeNames.DEFAULT);
        if (StringUtils.hasText(generalExporterTypeString)) {
            return ExporterType.valueOf(generalExporterTypeString.toUpperCase())
                    .toString()
                    .equalsIgnoreCase(requestedExporterType);
        }

        return false;
    }
}
