package io.zhijun.devservice.boot.autoconfigure.openlit;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenLitDevServiceProperties}.
 */
class OpenLitDevServicePropertiesTests extends BaseDevServicePropertiesTests<OpenLitDevServiceProperties> {

    @Override
    protected OpenLitDevServiceProperties createProperties() {
        return new OpenLitDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseOpenLitContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .startupTimeout(Duration.ofMinutes(2))
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        OpenLitDevServiceProperties properties = createProperties();

        assertThat(properties.getOtlpGrpcPort()).isEqualTo(0);
        assertThat(properties.getOtlpHttpPort()).isEqualTo(0);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        OpenLitDevServiceProperties properties = createProperties();

        properties.setOtlpGrpcPort(9001);
        properties.setOtlpHttpPort(9002);

        assertThat(properties.getOtlpGrpcPort()).isEqualTo(9001);
        assertThat(properties.getOtlpHttpPort()).isEqualTo(9002);
    }

}
