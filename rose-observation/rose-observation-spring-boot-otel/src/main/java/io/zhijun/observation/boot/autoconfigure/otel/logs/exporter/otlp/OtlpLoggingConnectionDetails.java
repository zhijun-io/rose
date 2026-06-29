package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter.otlp;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpConnectionDetails;

/**
 * Connection details to establish a connection to an OTLP endpoint for logging.
 */
@FunctionalInterface
public interface OtlpLoggingConnectionDetails extends OtlpConnectionDetails {

    String LOGS_PATH = "/v1/logs";

    String DEFAULT_GRPC_ENDPOINT = "http://localhost:" + DEFAULT_GRPC_PORT;
    String DEFAULT_HTTP_PROTOBUF_ENDPOINT = "http://localhost:" + DEFAULT_HTTP_PORT + LOGS_PATH;
}
