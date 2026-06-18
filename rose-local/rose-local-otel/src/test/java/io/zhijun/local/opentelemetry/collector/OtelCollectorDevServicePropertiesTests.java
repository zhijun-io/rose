package io.zhijun.local.opentelemetry.collector;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OtelCollectorLocalServiceProperties}.
 */
class OtelCollectorDevServicePropertiesTests extends BaseLocalServicePropertiesTests<OtelCollectorLocalServiceProperties> {

    @Override
    protected OtelCollectorLocalServiceProperties createProperties() {
        return new OtelCollectorLocalServiceProperties();
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
        OtelCollectorLocalServiceProperties properties = createProperties();
        assertThat(properties.getOtlpGrpcPort()).isEqualTo(0);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        OtelCollectorLocalServiceProperties properties = createProperties();
        properties.setOtlpGrpcPort(RoseOtelCollectorContainer.OTLP_GRPC_PORT);
        assertThat(properties.getOtlpGrpcPort()).isEqualTo(RoseOtelCollectorContainer.OTLP_GRPC_PORT);
    }

}
