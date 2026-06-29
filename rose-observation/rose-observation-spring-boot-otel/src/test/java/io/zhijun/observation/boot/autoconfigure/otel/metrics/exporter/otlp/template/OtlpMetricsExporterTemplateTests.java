package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

class OtlpMetricsExporterTemplateTests {

    private final OtlpMetricsExporterTemplate template = new OtlpMetricsExporterTemplate();

    @Test
    void shouldBuildHttpAndGrpcMetricExporters() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();
        OtlpMetricsConnectionDetails connectionDetails = mock(OtlpMetricsConnectionDetails.class);
        when(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF)).thenReturn("http://localhost:4318/v1/metrics");
        when(connectionDetails.getUrl(Protocol.GRPC)).thenReturn("http://localhost:4317");

        OtlpHttpMetricExporter httpExporter =
                template.buildHttpMetricExporter(commonProperties, properties, connectionDetails);
        OtlpGrpcMetricExporter grpcExporter =
                template.buildGrpcMetricExporter(commonProperties, properties, connectionDetails);

        assertThat(httpExporter).isNotNull();
        assertThat(grpcExporter).isNotNull();
    }
}
