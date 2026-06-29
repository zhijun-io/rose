package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.zhijun.observation.boot.autoconfigure.otel.logs.ConditionalOnOpenTelemetryLogging;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.console.ConsoleLoggingExporterConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp.OtlpLoggingExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry logs.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryLogging
@Import({ConsoleLoggingExporterConfiguration.class, OtlpLoggingExporterConfiguration.class})
@EnableConfigurationProperties(OpenTelemetryLoggingExporterProperties.class)
public final class OpenTelemetryLoggingExporterAutoConfiguration {}
