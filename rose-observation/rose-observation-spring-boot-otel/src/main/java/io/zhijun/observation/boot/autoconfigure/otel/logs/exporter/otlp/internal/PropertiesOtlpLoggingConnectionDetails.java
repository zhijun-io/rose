package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp.internal;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.OpenTelemetryLoggingExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp.OtlpLoggingConnectionDetails;

/**
 * Property-backed {@link OtlpLoggingConnectionDetails} implementation.
 */
public final class PropertiesOtlpLoggingConnectionDetails implements OtlpLoggingConnectionDetails {

    private final OpenTelemetryExporterProperties commonProperties;
    private final OpenTelemetryLoggingExporterProperties properties;

    public PropertiesOtlpLoggingConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryLoggingExporterProperties properties) {
        this.commonProperties = commonProperties;
        this.properties = properties;
    }

    @Override
    public String getUrl(Protocol protocol) {
        return OtlpConnectionUrls.resolve(
                protocol,
                commonProperties,
                properties.getOtlp(),
                LOGS_PATH,
                DEFAULT_HTTP_PROTOBUF_ENDPOINT,
                DEFAULT_GRPC_ENDPOINT);
    }
}
