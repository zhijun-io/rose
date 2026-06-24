package io.zhijun.observability.otel.autoconfigure.logs.exporter;

import org.junit.jupiter.api.Test;

import io.zhijun.observability.otel.autoconfigure.exporter.ExporterType;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpExporterConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryLoggingExporterProperties}.
 */
class OpenTelemetryLoggingExporterPropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(OpenTelemetryLoggingExporterProperties.CONFIG_PREFIX).isEqualTo("rose.otel.logs.exporter");
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        OpenTelemetryLoggingExporterProperties properties = new OpenTelemetryLoggingExporterProperties();

        assertThat(properties.getType()).isNull();
        assertThat(properties.getOtlp()).isNotNull().isInstanceOf(OtlpExporterConfig.class);
    }

    @Test
    void shouldUpdateValues() {
        OpenTelemetryLoggingExporterProperties properties = new OpenTelemetryLoggingExporterProperties();
        properties.setType(ExporterType.NONE);
        assertThat(properties.getType()).isEqualTo(ExporterType.NONE);
    }

}
