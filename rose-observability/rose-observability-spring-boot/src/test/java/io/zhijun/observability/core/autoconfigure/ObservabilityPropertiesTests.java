package io.zhijun.observability.core.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(ObservabilityProperties.CONFIG_PREFIX).isEqualTo("rose.observability");
    }
}
