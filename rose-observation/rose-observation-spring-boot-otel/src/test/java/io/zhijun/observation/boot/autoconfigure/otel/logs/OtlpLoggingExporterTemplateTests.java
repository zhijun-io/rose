package io.zhijun.observation.boot.autoconfigure.otel.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggingExporterProperties;

class OtlpLoggingExporterTemplateTests {

    private final OtlpLoggingExporterTemplate template = new OtlpLoggingExporterTemplate();

    @Test
    void shouldBuildHttpAndGrpcLogExporters() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        OpenTelemetryLoggingExporterProperties properties = new OpenTelemetryLoggingExporterProperties();
        OtlpLoggingConnectionDetails connectionDetails = mock(OtlpLoggingConnectionDetails.class);
        when(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF)).thenReturn("http://localhost:4318/v1/logs");
        when(connectionDetails.getUrl(Protocol.GRPC)).thenReturn("http://localhost:4317");

        @SuppressWarnings("unchecked")
        ObjectProvider<MeterProvider> meterProvider = mock(ObjectProvider.class);

        OtlpHttpLogRecordExporter httpExporter =
                template.buildHttpLogExporter(commonProperties, properties, connectionDetails, meterProvider);
        OtlpGrpcLogRecordExporter grpcExporter =
                template.buildGrpcLogExporter(commonProperties, properties, connectionDetails, meterProvider);

        assertThat(httpExporter).isNotNull();
        assertThat(grpcExporter).isNotNull();
    }
}
