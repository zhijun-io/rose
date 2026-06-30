package io.zhijun.observation.boot.autoconfigure.otel.traces;

import io.zhijun.observation.boot.autoconfigure.otel.common.OtlpConnectionDetails;

/**
 * Connection details to establish a connection to an OTLP endpoint for tracing.
 */
@FunctionalInterface
public interface OtlpTracingConnectionDetails extends OtlpConnectionDetails {

    String TRACES_PATH = "/v1/traces";

    String DEFAULT_GRPC_ENDPOINT = "http://localhost:" + DEFAULT_GRPC_PORT;
    String DEFAULT_HTTP_PROTOBUF_ENDPOINT = "http://localhost:" + DEFAULT_HTTP_PORT + TRACES_PATH;
}
