package io.zhijun.opentelemetry.autoconfigure.exporter.otlp;

import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.otlp.OtlpTracingConnectionDetails;

/**
 * Builds OTLP connection details from dev service container endpoints (Boot 2.7 alternative to service connections).
 */
public final class OtlpContainerConnectionDetails {

    private OtlpContainerConnectionDetails() {
    }

    public static OtlpTracingConnectionDetails tracing(String host, int httpPort, int grpcPort) {
        return protocol -> {
            if (protocol == Protocol.HTTP_PROTOBUF) {
                return String.format("http://%s:%d%s", host, httpPort, OtlpTracingConnectionDetails.TRACES_PATH);
            }
            return String.format("http://%s:%d", host, grpcPort);
        };
    }

    public static OtlpMetricsConnectionDetails metrics(String host, int httpPort, int grpcPort) {
        return protocol -> {
            if (protocol == Protocol.HTTP_PROTOBUF) {
                return String.format("http://%s:%d%s", host, httpPort, OtlpMetricsConnectionDetails.METRICS_PATH);
            }
            return String.format("http://%s:%d", host, grpcPort);
        };
    }
}
