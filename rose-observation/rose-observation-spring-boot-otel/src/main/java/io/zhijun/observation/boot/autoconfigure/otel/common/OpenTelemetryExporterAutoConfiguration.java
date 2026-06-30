package io.zhijun.observation.boot.autoconfigure.otel.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.zhijun.observation.boot.autoconfigure.otel.ConditionalOnOpenTelemetry;

/**
 * Auto-configuration for OpenTelemetry exporters.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetry
@EnableConfigurationProperties(OpenTelemetryExporterProperties.class)
public final class OpenTelemetryExporterAutoConfiguration {}
