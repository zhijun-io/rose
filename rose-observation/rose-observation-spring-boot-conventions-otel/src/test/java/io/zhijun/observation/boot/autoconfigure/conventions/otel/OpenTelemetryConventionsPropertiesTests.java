package io.zhijun.observation.boot.autoconfigure.conventions.otel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryConventionsProperties}.
 */
class OpenTelemetryConventionsPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(OpenTelemetryConventionsProperties.CONFIG_PREFIX)
                .isEqualTo("rose.observation.conventions.otel");
    }

}
