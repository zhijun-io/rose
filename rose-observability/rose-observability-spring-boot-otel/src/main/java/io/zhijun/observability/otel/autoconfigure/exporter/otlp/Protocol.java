package io.zhijun.observability.otel.autoconfigure.exporter.otlp;

/**
 * Transport protocol to use for OTLP requests.
 */
public enum Protocol {

    GRPC,

	HTTP_PROTOBUF

}
