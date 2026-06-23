package io.zhijun.opentelemetry.autoconfigure.exporter.otlp;

/**
 * Connection details to establish a connection to an OTLP endpoint.
 */
public interface OtlpConnectionDetails {

    int DEFAULT_GRPC_PORT = 4317;
    int DEFAULT_HTTP_PORT = 4318;

    String getUrl(Protocol protocol);

}
