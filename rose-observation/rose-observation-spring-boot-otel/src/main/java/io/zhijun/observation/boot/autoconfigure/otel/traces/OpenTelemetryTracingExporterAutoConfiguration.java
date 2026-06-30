package io.zhijun.observation.boot.autoconfigure.otel.traces;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.zhijun.observation.boot.autoconfigure.otel.traces.ConditionalOnOpenTelemetryTracing;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OtlpTracingExporterConfiguration;

/**
 * Auto-configuration for exporting OpenTelemetry traces.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryTracing
@Import({ConsoleTracingExporterConfiguration.class, OtlpTracingExporterConfiguration.class})
@EnableConfigurationProperties(OpenTelemetryTracingExporterProperties.class)
public final class OpenTelemetryTracingExporterAutoConfiguration {}
