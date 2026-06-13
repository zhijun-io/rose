package io.zhijun.opentelemetry.autoconfigure.traces.exporter;

import org.junit.jupiter.api.Test;

import io.zhijun.opentelemetry.autoconfigure.exporter.ExporterType;
import io.zhijun.opentelemetry.autoconfigure.exporter.otlp.OtlpExporterConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenTelemetryTracingExporterProperties}.
 */
class OpenTelemetryTracingExporterPropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(OpenTelemetryTracingExporterProperties.CONFIG_PREFIX).isEqualTo("rose.otel.traces.exporter");
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        OpenTelemetryTracingExporterProperties properties = new OpenTelemetryTracingExporterProperties();

        assertThat(properties.getType()).isNull();
        assertThat(properties.getOtlp()).isNotNull().isInstanceOf(OtlpExporterConfig.class);
    }

    @Test
    void shouldUpdateValues() {
        OpenTelemetryTracingExporterProperties properties = new OpenTelemetryTracingExporterProperties();
        properties.setType(ExporterType.NONE);

        assertThat(properties.getType()).isEqualTo(ExporterType.NONE);
    }

}
