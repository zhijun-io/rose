package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

/**
 * Transport protocol to use for OTLP requests.
 */
public enum Protocol {

    GRPC,

	HTTP_PROTOBUF

}
