package io.zhijun.observability.otel.autoconfigure.logs.exporter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.zhijun.observability.otel.autoconfigure.logs.ConditionalOnOpenTelemetryLogging;
import io.zhijun.observability.otel.autoconfigure.logs.exporter.console.ConsoleLoggingExporterConfiguration;
import io.zhijun.observability.otel.autoconfigure.logs.exporter.otlp.OtlpLoggingExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry logs.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryLogging
@Import({ ConsoleLoggingExporterConfiguration.class, OtlpLoggingExporterConfiguration.class })
@EnableConfigurationProperties(OpenTelemetryLoggingExporterProperties.class)
public final class OpenTelemetryLoggingExporterAutoConfiguration {}
