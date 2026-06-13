package io.zhijun.opentelemetry.autoconfigure.logs.exporter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.zhijun.opentelemetry.autoconfigure.logs.ConditionalOnOpenTelemetryLogging;
import io.zhijun.opentelemetry.autoconfigure.logs.exporter.console.ConsoleLoggingExporterConfiguration;
import io.zhijun.opentelemetry.autoconfigure.logs.exporter.otlp.OtlpLoggingExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry logs.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryLogging
@Import({ ConsoleLoggingExporterConfiguration.class, OtlpLoggingExporterConfiguration.class })
@EnableConfigurationProperties(OpenTelemetryLoggingExporterProperties.class)
public final class OpenTelemetryLoggingExporterAutoConfiguration {}
