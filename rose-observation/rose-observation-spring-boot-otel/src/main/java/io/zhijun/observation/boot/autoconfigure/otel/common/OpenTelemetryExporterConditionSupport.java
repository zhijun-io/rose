package io.zhijun.observation.boot.autoconfigure.otel.common;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;



/**
 * Shared exporter-type resolution for traces, metrics, and logs OTLP conditions.
 */

public final class OpenTelemetryExporterConditionSupport {

    private static final String GENERAL_EXPORTER_TYPE = OpenTelemetryExporterProperties.TYPE_PROPERTY;

    private OpenTelemetryExporterConditionSupport() {}

    public static ConditionOutcome evaluate(
            ConditionContext context,
            AnnotatedTypeMetadata metadata,
            Class<? extends Annotation> annotationType,
            String signalExporterTypeProperty) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationType.getName());
        String requestedExporterType = attributes != null ? (String) attributes.get("value") : null;

        if (!StringUtils.hasText(requestedExporterType)) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(annotationType).because("a valid exporter type is not specified"));
        }

        String generalExporterTypeString =
                context.getEnvironment().getProperty(GENERAL_EXPORTER_TYPE, ExporterTypeNames.DEFAULT);
        ExporterType generalExporterType = StringUtils.hasText(generalExporterTypeString)
                ? ExporterType.valueOf(generalExporterTypeString.toUpperCase())
                : null;

        String signalExporterTypeString =
                context.getEnvironment().getProperty(signalExporterTypeProperty, String.class);
        ExporterType signalExporterType = StringUtils.hasText(signalExporterTypeString)
                ? ExporterType.valueOf(signalExporterTypeString.toUpperCase())
                : null;

        if (signalExporterType != null) {
            if (signalExporterType.toString().equalsIgnoreCase(requestedExporterType)) {
                return ConditionOutcome.match(ConditionMessage.forCondition(annotationType)
                        .because(signalExporterTypeProperty + " is set to " + signalExporterType));
            }
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(annotationType)
                    .because(signalExporterTypeProperty + " is set to " + signalExporterType + ", but requested "
                            + requestedExporterType));
        }

        if (generalExporterType != null) {
            if (generalExporterType.toString().equalsIgnoreCase(requestedExporterType)) {
                return ConditionOutcome.match(ConditionMessage.forCondition(annotationType)
                        .because(GENERAL_EXPORTER_TYPE + " is set to " + generalExporterType));
            }
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(annotationType)
                    .because(GENERAL_EXPORTER_TYPE + " is set to " + generalExporterType + ", but requested "
                            + requestedExporterType));
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(annotationType)
                .because("exporter type not enabled: " + requestedExporterType));
    }
}
