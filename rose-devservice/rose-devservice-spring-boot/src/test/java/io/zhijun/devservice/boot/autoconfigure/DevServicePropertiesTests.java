package io.zhijun.devservice.boot.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
