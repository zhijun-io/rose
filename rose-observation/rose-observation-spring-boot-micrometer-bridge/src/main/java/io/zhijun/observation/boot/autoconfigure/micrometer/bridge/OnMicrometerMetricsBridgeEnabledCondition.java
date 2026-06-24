package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
/**
 * Activates the Micrometer metrics bridge when OTLP Micrometer registry export is disabled
 * and either console or OTLP metrics export is enabled.
 */
class OnMicrometerMetricsBridgeEnabledCondition extends SpringBootCondition {

  private static final String OTLP_MICROMETER_ENABLED = "rose.otel.exporter.otlp.micrometer.enabled";

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    boolean otlpMicrometerEnabled = context.getEnvironment().getProperty(OTLP_MICROMETER_ENABLED, Boolean.class, false);
    if (otlpMicrometerEnabled) {
      return ConditionOutcome.noMatch(ConditionMessage.forCondition(getClass().getName())
          .because(OTLP_MICROMETER_ENABLED + " is true"));
    }

    if (isMetricsExporterEnabled(context, "console") || isMetricsExporterEnabled(context, "otlp")) {
      return ConditionOutcome.match(ConditionMessage.forCondition(getClass().getName())
          .because("console or otlp metrics exporter enabled"));
    }

    return ConditionOutcome.noMatch(ConditionMessage.forCondition(getClass().getName())
        .because("no supported metrics exporter enabled"));
  }

  private static boolean isMetricsExporterEnabled(ConditionContext context, String requestedExporterType) {
    String metricsExporterTypeString = context.getEnvironment()
        .getProperty(OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".type", String.class);
  if (StringUtils.hasText(metricsExporterTypeString)) {
      return metricsExporterTypeString.equalsIgnoreCase(requestedExporterType);
    }

    String generalExporterTypeString = context.getEnvironment()
        .getProperty(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".type", "otlp");
    if (StringUtils.hasText(generalExporterTypeString)) {
      return ExporterType.valueOf(generalExporterTypeString.toUpperCase()).toString()
          .equalsIgnoreCase(requestedExporterType);
    }

    return false;
  }

}
