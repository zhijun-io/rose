package io.zhijun.observation.boot.autoconfigure.otel.logs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

import io.zhijun.observation.boot.autoconfigure.otel.ConditionalOnOpenTelemetry;

/**
 * Whether OpenTelemetry logging annotation is eligible for registration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@ConditionalOnOpenTelemetry
@Conditional(OnOpenTelemetryLoggingCondition.class)
public @interface ConditionalOnOpenTelemetryLogging {
}
