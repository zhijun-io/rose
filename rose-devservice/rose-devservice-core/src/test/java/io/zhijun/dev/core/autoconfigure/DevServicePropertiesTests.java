package io.zhijun.dev.core.autoconfigure;

import io.zhijun.dev.core.autoconfigure.DevServiceProperties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DevServiceProperties}.
 */
class DevServicePropertiesTests {

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        DevServiceProperties properties = new DevServiceProperties();
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void shouldUpdateValues() {
        DevServiceProperties properties = new DevServiceProperties();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

}
