package io.zhijun.opentelemetry.autoconfigure.traces.exporter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.zhijun.opentelemetry.autoconfigure.traces.ConditionalOnOpenTelemetryTracing;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.console.ConsoleTracingExporterConfiguration;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.otlp.OtlpTracingExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry traces.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryTracing
@Import({ ConsoleTracingExporterConfiguration.class, OtlpTracingExporterConfiguration.class })
@EnableConfigurationProperties(OpenTelemetryTracingExporterProperties.class)
public final class OpenTelemetryTracingExporterAutoConfiguration {}
