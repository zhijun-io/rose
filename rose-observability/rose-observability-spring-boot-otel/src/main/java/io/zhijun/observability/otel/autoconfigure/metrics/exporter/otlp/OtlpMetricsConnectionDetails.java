package io.zhijun.observability.otel.autoconfigure.metrics.exporter.otlp;

import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpConnectionDetails;

/**
 * Connection details to establish a connection to an OTLP endpoint for metrics.
 */
public interface OtlpMetricsConnectionDetails extends OtlpConnectionDetails {

    String METRICS_PATH = "/v1/metrics";

    String DEFAULT_GRPC_ENDPOINT = "http://localhost:" + DEFAULT_GRPC_PORT;
    String DEFAULT_HTTP_PROTOBUF_ENDPOINT = "http://localhost:" + DEFAULT_HTTP_PORT + METRICS_PATH;

}
