package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

/**
 * Connection details to establish a connection to an OTLP endpoint.
 */
@FunctionalInterface
public interface OtlpConnectionDetails {

    int DEFAULT_GRPC_PORT = 4317;
    int DEFAULT_HTTP_PORT = 4318;

    String getUrl(Protocol protocol);
}
