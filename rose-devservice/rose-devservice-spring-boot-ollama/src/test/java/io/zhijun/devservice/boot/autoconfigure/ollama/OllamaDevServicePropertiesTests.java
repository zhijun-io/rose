package io.zhijun.devservice.boot.autoconfigure.ollama;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
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
                .imageName(OllamaDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .startupTimeout(BaseDevServiceProperties.HEAVY_STARTUP_TIMEOUT)
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
