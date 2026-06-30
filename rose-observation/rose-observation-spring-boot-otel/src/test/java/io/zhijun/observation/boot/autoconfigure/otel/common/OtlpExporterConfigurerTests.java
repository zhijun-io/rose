package io.zhijun.observation.boot.autoconfigure.otel.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;

/**
 * Unit test for {@link OtlpExporterConfigurer}.
 */
class OtlpExporterConfigurerTests {

    @Test
    void shouldPreferSignalMetricsFlagWhenPresent() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        commonProperties.getOtlp().setMetrics(true);

        OtlpExporterConfig signalProperties = new OtlpExporterConfig();
        signalProperties.setMetrics(false);

        assertThat(OtlpExporterConfigurer.isMetricsEnabled(commonProperties, signalProperties)).isFalse();
    }

    @Test
    void shouldUseCommonMetricsFlagWhenSignalFlagMissing() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        commonProperties.getOtlp().setMetrics(true);

        OtlpExporterConfig signalProperties = new OtlpExporterConfig();

        assertThat(OtlpExporterConfigurer.isMetricsEnabled(commonProperties, signalProperties)).isTrue();
    }

    @Test
    void shouldDisableMetricsWhenBothFlagsAreFalseOrMissing() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();

        OtlpExporterConfig signalProperties = new OtlpExporterConfig();
        signalProperties.setMetrics(false);

        assertThat(OtlpExporterConfigurer.isMetricsEnabled(commonProperties, signalProperties)).isFalse();
    }
}
