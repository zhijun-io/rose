package io.zhijun.observation.boot.autoconfigure.conventions.otel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the OpenTelemetry Semantic Conventions.
 */
@ConfigurationProperties(prefix = OpenTelemetryConventionsProperties.CONFIG_PREFIX)
public class OpenTelemetryConventionsProperties {

    public static final String CONFIG_PREFIX = "rose.observation.conventions.otel";
}
