package io.zhijun.observation.boot.autoconfigure.logback;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import ch.qos.logback.core.Appender;

import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.logs.ConditionalOnOpenTelemetryLoggingExporter;

/**
 * Auto-configuration for Logback OpenTelemetry Bridge.
 */
@AutoConfiguration(after = OpenTelemetryAutoConfiguration.class)
@ConditionalOnClass(Appender.class)
@ConditionalOnProperty(
        prefix = LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@ConditionalOnOpenTelemetryLoggingExporter("otlp")
@EnableConfigurationProperties(LogbackOpenTelemetryBridgeProperties.class)
public final class LogbackOpenTelemetryBridgeAutoConfiguration {

    @Bean
    @ConditionalOnBean(OpenTelemetry.class)
    ApplicationListener<ApplicationReadyEvent> logbackAppenderOnReady(OpenTelemetry openTelemetry) {
        return event -> installAppender(openTelemetry);
    }

    @Bean
    @ConditionalOnBean(OpenTelemetry.class)
    ApplicationListener<ApplicationFailedEvent> logbackAppenderOnFailed(OpenTelemetry openTelemetry) {
        return event -> installAppender(openTelemetry);
    }

    private static void installAppender(OpenTelemetry openTelemetry) {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
