package io.zhijun.devservice.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DevServiceProperties}.
 */
class DevServicePropertiesTests {

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        DevServiceProperties properties = new DevServiceProperties();
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldUpdateValues() {
        DevServiceProperties properties = new DevServiceProperties();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }
}
