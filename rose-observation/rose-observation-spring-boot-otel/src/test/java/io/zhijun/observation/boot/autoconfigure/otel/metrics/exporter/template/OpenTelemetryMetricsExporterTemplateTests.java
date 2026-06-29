package io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.template;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

class OpenTelemetryMetricsExporterTemplateTests {

    private final OpenTelemetryMetricsExporterTemplate template = new OpenTelemetryMetricsExporterTemplate();

    @Test
    void buildsMetricReaderCustomizer() {
        OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();
        ObjectProvider<MetricExporter> exporters = mock(ObjectProvider.class);
        when(exporters.orderedStream()).thenReturn(Stream.of(mock(MetricExporter.class)));

        OpenTelemetryMeterProviderBuilderCustomizer customizer =
                template.metricReaderCustomizer(properties, exporters);
        SdkMeterProviderBuilder builder = SdkMeterProvider.builder();

        assertThatCode(() -> customizer.customize(builder)).doesNotThrowAnyException();
    }
}
