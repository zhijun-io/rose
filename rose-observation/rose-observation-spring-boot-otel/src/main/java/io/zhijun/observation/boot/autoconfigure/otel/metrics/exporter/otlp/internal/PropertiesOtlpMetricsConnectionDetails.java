package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.internal;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;

/**
 * Property-backed {@link OtlpMetricsConnectionDetails} implementation.
 */
public final class PropertiesOtlpMetricsConnectionDetails implements OtlpMetricsConnectionDetails {

    private final OpenTelemetryExporterProperties commonProperties;
    private final OpenTelemetryMetricsExporterProperties properties;

    public PropertiesOtlpMetricsConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties) {
        this.commonProperties = commonProperties;
        this.properties = properties;
    }

    @Override
    public String getUrl(Protocol protocol) {
        return OtlpConnectionUrls.resolve(
                protocol,
                commonProperties,
                properties.getOtlp(),
                METRICS_PATH,
                DEFAULT_HTTP_PROTOBUF_ENDPOINT,
                DEFAULT_GRPC_ENDPOINT);
    }
}
