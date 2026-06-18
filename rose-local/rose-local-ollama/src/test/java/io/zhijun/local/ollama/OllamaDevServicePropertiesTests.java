package io.zhijun.local.ollama;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OllamaLocalServiceProperties}.
 */
class OllamaDevServicePropertiesTests extends BaseLocalServicePropertiesTests<OllamaLocalServiceProperties> {

    @Override
    protected OllamaLocalServiceProperties createProperties() {
        return new OllamaLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseOllamaContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .startupTimeout(Duration.ofMinutes(2))
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        OllamaLocalServiceProperties properties = createProperties();
        assertThat(properties.isIgnoreNativeService()).isFalse();
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        OllamaLocalServiceProperties properties = createProperties();
        properties.setIgnoreNativeService(true);
        assertThat(properties.isIgnoreNativeService()).isTrue();
    }

}
