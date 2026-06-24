package io.zhijun.devservice.boot.autoconfigure.otel;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OtelCollectorDevServiceProperties}.
 */
class OtelCollectorDevServicePropertiesTests extends BaseDevServicePropertiesTests<OtelCollectorDevServiceProperties> {

    @Override
    protected OtelCollectorDevServiceProperties createProperties() {
        return new OtelCollectorDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseOtelCollectorContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        OtelCollectorDevServiceProperties properties = createProperties();
        assertThat(properties.getOtlpGrpcPort()).isEqualTo(0);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        OtelCollectorDevServiceProperties properties = createProperties();
        properties.setOtlpGrpcPort(RoseOtelCollectorContainer.OTLP_GRPC_PORT);
        assertThat(properties.getOtlpGrpcPort()).isEqualTo(RoseOtelCollectorContainer.OTLP_GRPC_PORT);
    }

}
