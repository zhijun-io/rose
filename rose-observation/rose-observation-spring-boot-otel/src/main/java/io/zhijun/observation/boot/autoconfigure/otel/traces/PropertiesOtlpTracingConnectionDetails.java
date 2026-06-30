package io.zhijun.observation.boot.autoconfigure.otel.traces;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OtlpTracingConnectionDetails;

/**
 * Property-backed {@link OtlpTracingConnectionDetails} implementation.
 */
public final class PropertiesOtlpTracingConnectionDetails implements OtlpTracingConnectionDetails {

    private final OpenTelemetryExporterProperties commonProperties;
    private final OpenTelemetryTracingExporterProperties properties;

    public PropertiesOtlpTracingConnectionDetails(
            OpenTelemetryExporterProperties commonProperties, OpenTelemetryTracingExporterProperties properties) {
        this.commonProperties = commonProperties;
        this.properties = properties;
    }

    @Override
    public String getUrl(Protocol protocol) {
        return OtlpConnectionUrls.resolve(
                protocol,
                commonProperties,
                properties.getOtlp(),
                TRACES_PATH,
                DEFAULT_HTTP_PROTOBUF_ENDPOINT,
                DEFAULT_GRPC_ENDPOINT);
    }
}
