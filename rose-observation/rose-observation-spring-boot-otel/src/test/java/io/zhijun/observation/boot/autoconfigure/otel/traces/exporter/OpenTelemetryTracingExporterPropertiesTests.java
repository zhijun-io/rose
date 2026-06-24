package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryTracingExporterProperties}.
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
