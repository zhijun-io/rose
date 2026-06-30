package io.zhijun.observation.boot.autoconfigure.otel.common;

/**
 * Transport protocol to use for OTLP requests.
 */
public enum Protocol {
    GRPC,

    HTTP_PROTOBUF
}
