package io.zhijun.observability.conventions.otel.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryConventionsProperties}.
 */
class OpenTelemetryConventionsPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(OpenTelemetryConventionsProperties.CONFIG_PREFIX)
                .isEqualTo("rose.observability.conventions.otel");
    }

}
