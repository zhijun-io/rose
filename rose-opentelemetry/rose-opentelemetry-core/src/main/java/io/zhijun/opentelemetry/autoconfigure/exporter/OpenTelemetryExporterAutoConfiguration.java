package io.zhijun.opentelemetry.autoconfigure.exporter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.zhijun.opentelemetry.autoconfigure.ConditionalOnOpenTelemetry;

/**
 * Auto-configuration for OpenTelemetry exporters.
 */
@AutoConfiguration
@ConditionalOnOpenTelemetry
@EnableConfigurationProperties(OpenTelemetryExporterProperties.class)
public final class OpenTelemetryExporterAutoConfiguration {

}
