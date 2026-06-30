package io.zhijun.observation.boot.autoconfigure.otel.logs;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.OtlpConnectionUrls;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggingExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OtlpLoggingConnectionDetails;

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
