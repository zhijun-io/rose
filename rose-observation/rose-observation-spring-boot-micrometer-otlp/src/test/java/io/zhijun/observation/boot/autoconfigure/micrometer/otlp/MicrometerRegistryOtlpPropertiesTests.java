package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;

/**
 * Unit test for {@link MicrometerRegistryOtlpProperties}.
 */
class MicrometerRegistryOtlpPropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(MicrometerRegistryOtlpProperties.CONFIG_PREFIX)
                .isEqualTo(OpenTelemetryExporterProperties.MICROMETER_REGISTRY_CONFIG_PREFIX);
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        MicrometerRegistryOtlpProperties properties = new MicrometerRegistryOtlpProperties();

        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void shouldUpdateValue() {
        MicrometerRegistryOtlpProperties properties = new MicrometerRegistryOtlpProperties();

        properties.setEnabled(false);

        assertThat(properties.isEnabled()).isFalse();
    }
}
