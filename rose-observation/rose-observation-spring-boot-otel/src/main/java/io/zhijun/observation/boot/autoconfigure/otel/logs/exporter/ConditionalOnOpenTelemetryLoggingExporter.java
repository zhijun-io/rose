package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

import io.zhijun.observation.boot.autoconfigure.otel.logs.ConditionalOnOpenTelemetryLogging;

/**
 * Whether logs should be exported through the configured OpenTelemetry exporter type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnOpenTelemetryLogging
@Conditional(OnOpenTelemetryLoggingExporterCondition.class)
public @interface ConditionalOnOpenTelemetryLoggingExporter {

    /**
     * The exporter type name (for example {@code otlp}).
     */
    String value();
}
