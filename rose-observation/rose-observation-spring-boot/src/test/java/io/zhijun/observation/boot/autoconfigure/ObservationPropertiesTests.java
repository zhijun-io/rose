package io.zhijun.observation.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ObservationPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(ObservationProperties.CONFIG_PREFIX).isEqualTo("rose.observation");
    }
}
