package io.zhijun.devservice.ollama;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OllamaDevServiceProperties}.
 */
class OllamaDevServicePropertiesTests extends BaseDevServicePropertiesTests<OllamaDevServiceProperties> {

    @Override
    protected OllamaDevServiceProperties createProperties() {
        return new OllamaDevServiceProperties();
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
        OllamaDevServiceProperties properties = createProperties();
        assertThat(properties.isIgnoreNativeService()).isFalse();
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        OllamaDevServiceProperties properties = createProperties();
        properties.setIgnoreNativeService(true);
        assertThat(properties.isIgnoreNativeService()).isTrue();
    }

}
