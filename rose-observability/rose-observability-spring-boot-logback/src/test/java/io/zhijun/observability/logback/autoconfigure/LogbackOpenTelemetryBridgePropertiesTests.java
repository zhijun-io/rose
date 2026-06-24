package io.zhijun.observability.logback.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link LogbackOpenTelemetryBridgeProperties}.
 */
class LogbackOpenTelemetryBridgePropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX)
                .isEqualTo("rose.otel.logs.logback-bridge");
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        LogbackOpenTelemetryBridgeProperties properties = new LogbackOpenTelemetryBridgeProperties();
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void shouldUpdateEnabled() {
        LogbackOpenTelemetryBridgeProperties properties = new LogbackOpenTelemetryBridgeProperties();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

}
