package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.opentelemetry.sdk.resources.Resource;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsConnectionDetails;

/**
 * Builds Micrometer OTLP registry configuration.
 */
public final class MicrometerRegistryOtlpTemplate {

    private static final Set<String> RESERVED_RESOURCE_ATTRIBUTES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("telemetry.sdk.language", "telemetry.sdk.name", "telemetry.sdk.version")));

    MicrometerOtlpConfig build(
            OtlpMetricsConnectionDetails connectionDetails,
            OpenTelemetryExporterProperties commonProperties,
            OpenTelemetryMetricsExporterProperties metricsProperties,
            Resource resource) {
        Protocol protocol = metricsProperties.getOtlp().getProtocol() != null
                ? metricsProperties.getOtlp().getProtocol()
                : commonProperties.getOtlp().getProtocol();
        return MicrometerOtlpConfig.builder()
                .url(connectionDetails.getUrl(protocol))
                .step(metricsProperties.getInterval())
                .addResourceAttributes(resourceAttributes(resource))
                .build();
    }

    private static HashMap<String, String> resourceAttributes(Resource resource) {
        return resource.getAttributes().asMap().entrySet().stream()
                .filter(entry -> !RESERVED_RESOURCE_ATTRIBUTES.contains(entry.getKey().getKey()))
                .collect(
                        HashMap::new,
                        (attributes, entry) -> attributes.put(entry.getKey().getKey(), entry.getValue().toString()),
                        HashMap::putAll);
    }
}
