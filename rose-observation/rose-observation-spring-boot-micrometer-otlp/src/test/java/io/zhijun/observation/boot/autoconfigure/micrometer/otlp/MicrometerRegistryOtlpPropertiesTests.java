package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MicrometerRegistryOtlpProperties}.
 */
class MicrometerRegistryOtlpPropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(MicrometerRegistryOtlpProperties.CONFIG_PREFIX)
                .isEqualTo("rose.otel.exporter.otlp.micrometer");
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
