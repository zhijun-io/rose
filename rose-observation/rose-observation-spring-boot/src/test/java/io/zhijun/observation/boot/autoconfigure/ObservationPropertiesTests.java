package io.zhijun.observation.boot.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(ObservationProperties.CONFIG_PREFIX).isEqualTo("rose.observation");
    }
}
