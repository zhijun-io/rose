package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.OtlpTracingConnectionDetails;

class OtlpContainerConnectionDetailsTests {

    @Test
    void tracingHttpUrlIncludesTracesPath() {
        OtlpTracingConnectionDetails details = OtlpContainerConnectionDetails.tracing("localhost", 4318, 4317);
        assertThat(details.getUrl(Protocol.HTTP_PROTOBUF)).isEqualTo("http://localhost:4318/v1/traces");
        assertThat(details.getUrl(Protocol.GRPC)).isEqualTo("http://localhost:4317");
    }

    @Test
    void metricsHttpUrlIncludesMetricsPath() {
        OtlpMetricsConnectionDetails details = OtlpContainerConnectionDetails.metrics("localhost", 4318, 4317);
        assertThat(details.getUrl(Protocol.HTTP_PROTOBUF)).isEqualTo("http://localhost:4318/v1/metrics");
        assertThat(details.getUrl(Protocol.GRPC)).isEqualTo("http://localhost:4317");
    }
}
