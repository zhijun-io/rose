package io.zhijun.observation.boot.autoconfigure.otel.logs.exporter;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpExporterConfig;

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
