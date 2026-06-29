package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.OtlpTracingConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterProperties;

class OtlpTracingExporterTemplateTests {

    private final OtlpTracingExporterTemplate template = new OtlpTracingExporterTemplate();

    @Test
    void shouldBuildHttpAndGrpcTraceExporters() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        OpenTelemetryTracingExporterProperties properties = new OpenTelemetryTracingExporterProperties();
        OtlpTracingConnectionDetails connectionDetails = mock(OtlpTracingConnectionDetails.class);
        when(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF)).thenReturn("http://localhost:4318/v1/traces");
        when(connectionDetails.getUrl(Protocol.GRPC)).thenReturn("http://localhost:4317");

        @SuppressWarnings("unchecked")
        ObjectProvider<MeterProvider> meterProvider = mock(ObjectProvider.class);

        OtlpHttpSpanExporter httpExporter =
                template.buildHttpSpanExporter(commonProperties, properties, connectionDetails, meterProvider);
        OtlpGrpcSpanExporter grpcExporter =
                template.buildGrpcSpanExporter(commonProperties, properties, connectionDetails, meterProvider);

        assertThat(httpExporter).isNotNull();
        assertThat(grpcExporter).isNotNull();
    }
}
